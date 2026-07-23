package com.exam.cloud.exam.service.impl;

import com.exam.cloud.common.api.Result;
import com.exam.cloud.common.api.client.PaperClient;
import com.exam.cloud.common.api.client.QuestionClient;
import com.exam.cloud.common.api.client.ScoreClient;
import com.exam.cloud.common.api.client.WrongClient;
import com.exam.cloud.common.api.dto.ScoreCreateDTO;
import com.exam.cloud.common.api.dto.WrongBatchDTO;
import com.exam.cloud.common.constant.RedisKey;
import com.exam.cloud.common.entity.*;
import com.exam.cloud.exam.mapper.AnswerRecordMapper;
import com.exam.cloud.exam.mapper.ExamRecordMapper;
import com.exam.cloud.exam.retry.RetryService;
import com.exam.cloud.exam.service.ExamService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 考试服务（编排核心）：
 * - 自有 exam_record / answer_record 表
 * - 试卷与题目经 Feign 向 paper/question 服务读取
 * - 交卷后向 score / wrong 服务写入，失败入重试队列（最佳努力）
 * - Redis 维护考试进度缓存与并发交卷锁
 */
@Service
public class ExamServiceImpl implements ExamService {

    private static final Logger log = LoggerFactory.getLogger(ExamServiceImpl.class);

    private final ExamRecordMapper examRecordMapper;
    private final AnswerRecordMapper answerRecordMapper;
    private final PaperClient paperClient;
    private final QuestionClient questionClient;
    private final ScoreClient scoreClient;
    private final WrongClient wrongClient;
    private final StringRedisTemplate redis;
    private final TransactionTemplate txTemplate;
    private final RetryService retryService;

    public ExamServiceImpl(ExamRecordMapper examRecordMapper,
                           AnswerRecordMapper answerRecordMapper,
                           PaperClient paperClient,
                           QuestionClient questionClient,
                           ScoreClient scoreClient,
                           WrongClient wrongClient,
                           StringRedisTemplate redis,
                           TransactionTemplate txTemplate,
                           RetryService retryService) {
        this.examRecordMapper = examRecordMapper;
        this.answerRecordMapper = answerRecordMapper;
        this.paperClient = paperClient;
        this.questionClient = questionClient;
        this.scoreClient = scoreClient;
        this.wrongClient = wrongClient;
        this.redis = redis;
        this.txTemplate = txTemplate;
        this.retryService = retryService;
    }

    @Override
    public Map<String, Object> startExam(Long userId, Long paperId) {
        // 1. 校验试卷（Feign）
        Paper paper = fetchPaper(paperId);
        if (paper == null) throw new RuntimeException("试卷不存在");
        if (!"published".equals(paper.getStatus())) throw new RuntimeException("试卷未发布，无法考试");

        // 2. 检查进行中的考试（断点续考）
        ExamRecord inProgress = examRecordMapper.findInProgress(userId, paperId);
        ExamRecord record;
        boolean resume;
        if (inProgress != null) {
            record = inProgress;
            resume = true;
        } else {
            record = new ExamRecord();
            record.setUserId(userId);
            record.setPaperId(paperId);
            record.setStatus("in_progress");
            record.setStartTime(LocalDateTime.now());
            examRecordMapper.insert(record);
            // 写入 Redis 进度：开始时间
            String pkey = progressKey(userId, record.getId());
            redis.opsForHash().put(pkey, "startTime", record.getStartTime().toString());
            redis.expire(pkey, Duration.ofHours(24));
            resume = false;
        }

        Map<String, Object> result = buildExamData(record, paper);
        result.put("resume", resume);
        return result;
    }

    @Override
    public boolean submitAnswer(Long userId, Long examRecordId, Long questionId, String userAnswer) {
        ExamRecord exam = examRecordMapper.selectById(examRecordId);
        if (exam == null) throw new RuntimeException("考试记录不存在");
        if (!exam.getUserId().equals(userId)) throw new RuntimeException("无权操作");
        if (!"in_progress".equals(exam.getStatus())) throw new RuntimeException("考试已结束");

        // 校验题目属于该试卷（Feign）
        List<PaperQuestion> pqs = fetchPaperQuestions(exam.getPaperId());
        boolean valid = pqs.stream().anyMatch(pq -> pq.getQuestionId().equals(questionId));
        if (!valid) throw new RuntimeException("该题目不属于本试卷");

        // 本地 upsert 答案
        AnswerRecord answer = answerRecordMapper.findOne(examRecordId, questionId);
        if (answer != null) {
            answer.setUserAnswer(userAnswer);
            answerRecordMapper.updateById(answer);
        } else {
            answer = new AnswerRecord();
            answer.setExamRecordId(examRecordId);
            answer.setQuestionId(questionId);
            answer.setUserAnswer(userAnswer);
            answer.setIsCorrect(0);
            answer.setScore(0);
            answerRecordMapper.insert(answer);
        }

        // 同步 Redis 进度缓存（断点续考快速恢复）
        String pkey = progressKey(userId, examRecordId);
        redis.opsForHash().put(pkey, "q:" + questionId, userAnswer == null ? "" : userAnswer);
        redis.expire(pkey, Duration.ofHours(24));
        return true;
    }

    @Override
    public Map<String, Object> submitExam(Long userId, Long examRecordId) {
        // 1. 并发交卷锁
        String lockKey = RedisKey.EXAM_SUBMIT_LOCK + examRecordId;
        Boolean locked = redis.opsForValue().setIfAbsent(lockKey, "1", Duration.ofSeconds(60));
        if (locked == null || !locked) {
            throw new RuntimeException("正在提交中，请勿重复提交");
        }

        try {
            // 2. 校验
            ExamRecord exam = examRecordMapper.selectById(examRecordId);
            if (exam == null) throw new RuntimeException("考试记录不存在");
            if (!exam.getUserId().equals(userId)) throw new RuntimeException("无权操作");
            if (!"in_progress".equals(exam.getStatus())) throw new RuntimeException("考试已提交过");

            // 3. 读试卷题目与题目本体（Feign）——读失败则交卷失败，记录保持 in_progress
            Paper paper = fetchPaper(exam.getPaperId());
            List<PaperQuestion> pqs = fetchPaperQuestions(exam.getPaperId());
            List<Long> qids = pqs.stream().map(PaperQuestion::getQuestionId).distinct().collect(Collectors.toList());
            Map<Long, Question> qmap = fetchQuestions(qids);

            // 4. 本地事务：批改 + 更新答案 + 更新考试记录
            GradedResult graded = txTemplate.execute(status -> gradeAndPersist(examRecordId, pqs, qmap));

            // 5. 跨服务最佳努力：写成绩 + 维护错题；失败入重试队列
            bestEffortWriteScore(exam, graded);
            bestEffortWriteWrong(userId, examRecordId, pqs, qmap, graded);

            // 6. 清理 Redis 进度
            redis.delete(progressKey(userId, examRecordId));

            // 7. 返回（保持与旧版一致的结构）
            Map<String, Object> result = new HashMap<>();
            result.put("examRecordId", exam.getId());
            result.put("totalScore", graded.totalScore);
            result.put("maxScore", paper != null ? paper.getTotalScore() : graded.totalScore);
            result.put("correctCount", graded.correctCount);
            result.put("totalCount", pqs.size());
            result.put("answers", graded.details);
            return result;
        } finally {
            redis.delete(lockKey);
        }
    }

    @Override
    public Map<String, Object> getExamDetail(Long examRecordId) {
        ExamRecord exam = examRecordMapper.selectById(examRecordId);
        if (exam == null) throw new RuntimeException("考试记录不存在");

        Paper paper = fetchPaper(exam.getPaperId());
        List<AnswerRecord> answers = answerRecordMapper.findByExamRecordId(examRecordId);

        List<Long> qids = answers.stream().map(AnswerRecord::getQuestionId).distinct().collect(Collectors.toList());
        Map<Long, Question> qmap = fetchQuestions(qids);

        Map<String, Object> result = new HashMap<>();
        result.put("exam", exam);
        result.put("paperTitle", paper != null ? paper.getTitle() : "已删除");
        result.put("answers", buildAnswerDetailList(answers, qmap));
        return result;
    }

    @Override
    public List<Map<String, Object>> getMyExamList(Long userId) {
        List<ExamRecord> records = examRecordMapper.findByUserId(userId);
        List<Map<String, Object>> result = new ArrayList<>();
        for (ExamRecord r : records) {
            Paper p = fetchPaper(r.getPaperId());
            Map<String, Object> item = new HashMap<>();
            item.put("examRecordId", r.getId());
            item.put("paperId", r.getPaperId());
            item.put("paperTitle", p != null ? p.getTitle() : "已删除");
            item.put("status", r.getStatus());
            item.put("totalScore", r.getTotalScore());
            item.put("startTime", r.getStartTime());
            item.put("submitTime", r.getSubmitTime());
            result.add(item);
        }
        return result;
    }

    // ==================== 私有：批改与本地持久化 ====================

    /** 在本地事务内批改并写库，返回批改结果 */
    private GradedResult gradeAndPersist(Long examRecordId, List<PaperQuestion> pqs, Map<Long, Question> qmap) {
        int correctCount = 0;
        int totalScore = 0;
        List<Map<String, Object>> gradedAnswers = new ArrayList<>();

        for (PaperQuestion pq : pqs) {
            Question q = qmap.get(pq.getQuestionId());
            AnswerRecord answer = answerRecordMapper.findOne(examRecordId, pq.getQuestionId());
            String userAnswer = (answer != null) ? answer.getUserAnswer() : "";

            boolean correct = q != null && gradeQuestion(q, userAnswer);
            int questionScore = correct ? pq.getScore() : 0;

            if (answer != null) {
                answer.setIsCorrect(correct ? 1 : 0);
                answer.setScore(questionScore);
                answerRecordMapper.updateById(answer);
            } else {
                answer = new AnswerRecord();
                answer.setExamRecordId(examRecordId);
                answer.setQuestionId(pq.getQuestionId());
                answer.setUserAnswer("");
                answer.setIsCorrect(0);
                answer.setScore(0);
                answerRecordMapper.insert(answer);
            }

            if (correct) correctCount++;
            totalScore += questionScore;

            Map<String, Object> detail = new HashMap<>();
            detail.put("questionId", pq.getQuestionId());
            detail.put("type", q != null ? q.getType() : "");
            detail.put("content", q != null ? q.getContent() : "已删除");
            detail.put("optionA", q != null ? q.getOptionA() : null);
            detail.put("optionB", q != null ? q.getOptionB() : null);
            detail.put("optionC", q != null ? q.getOptionC() : null);
            detail.put("optionD", q != null ? q.getOptionD() : null);
            detail.put("userAnswer", userAnswer);
            detail.put("correctAnswer", q != null ? q.getAnswer() : "");
            detail.put("isCorrect", correct);
            detail.put("score", questionScore);
            detail.put("maxScore", pq.getScore());
            gradedAnswers.add(detail);
        }

        // 更新考试记录
        ExamRecord exam = examRecordMapper.selectById(examRecordId);
        exam.setStatus("submitted");
        exam.setSubmitTime(LocalDateTime.now());
        exam.setTotalScore(totalScore);
        examRecordMapper.updateById(exam);

        GradedResult g = new GradedResult();
        g.totalScore = totalScore;
        g.correctCount = correctCount;
        g.submitTime = exam.getSubmitTime();
        g.details = gradedAnswers;
        return g;
    }

    // ==================== 私有：跨服务最佳努力 ====================

    private void bestEffortWriteScore(ExamRecord exam, GradedResult graded) {
        ScoreCreateDTO dto = new ScoreCreateDTO(exam.getUserId(), exam.getPaperId(),
                graded.totalScore, exam.getId(), graded.submitTime);
        try {
            Result<Boolean> r = scoreClient.create(dto);
            if (r == null || (r.getCode() != null && r.getCode() != 200)) {
                throw new RuntimeException("score 返回非成功");
            }
        } catch (Exception e) {
            log.warn("写入成绩失败，入重试队列 examRecordId={}: {}", exam.getId(), e.getMessage());
            retryService.enqueueScore(dto);
        }
    }

    private void bestEffortWriteWrong(Long userId, Long examRecordId,
                                     List<PaperQuestion> pqs, Map<Long, Question> qmap, GradedResult graded) {
        List<WrongBatchDTO.Item> items = new ArrayList<>();
        for (Map<String, Object> d : graded.details) {
            Long qid = (Long) d.get("questionId");
            Boolean correct = (Boolean) d.get("isCorrect");
            items.add(new WrongBatchDTO.Item(qid, correct));
        }
        WrongBatchDTO dto = new WrongBatchDTO(userId, examRecordId, items);
        try {
            Result<Boolean> r = wrongClient.submitBatch(dto);
            if (r == null || (r.getCode() != null && r.getCode() != 200)) {
                throw new RuntimeException("wrong 返回非成功");
            }
        } catch (Exception e) {
            log.warn("维护错题失败，入重试队列 examRecordId={}: {}", examRecordId, e.getMessage());
            retryService.enqueueWrong(dto);
        }
    }

    // ==================== 私有：Feign 读取 ====================

    private Paper fetchPaper(Long paperId) {
        try {
            Result<Paper> r = paperClient.getById(paperId);
            return r != null ? r.getData() : null;
        } catch (Exception e) {
            log.warn("读取试卷失败 paperId={}: {}", paperId, e.getMessage());
            return null;
        }
    }

    private List<PaperQuestion> fetchPaperQuestions(Long paperId) {
        Result<List<PaperQuestion>> r = paperClient.getPaperQuestions(paperId);
        if (r == null || r.getData() == null) return Collections.emptyList();
        return r.getData();
    }

    private Map<Long, Question> fetchQuestions(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return Collections.emptyMap();
        try {
            Result<List<Question>> r = questionClient.batch(ids);
            if (r == null || r.getData() == null) return Collections.emptyMap();
            Map<Long, Question> map = new HashMap<>();
            for (Question q : r.getData()) map.put(q.getId(), q);
            return map;
        } catch (Exception e) {
            log.warn("批量读取题目失败 ids={}: {}", ids, e.getMessage());
            return Collections.emptyMap();
        }
    }

    // ==================== 私有：构建响应 ====================

    private Map<String, Object> buildExamData(ExamRecord record, Paper paper) {
        List<PaperQuestion> pqs = fetchPaperQuestions(paper.getId());
        List<Long> qids = pqs.stream().map(PaperQuestion::getQuestionId).distinct().collect(Collectors.toList());
        Map<Long, Question> qmap = fetchQuestions(qids);

        // 已保存答案（优先 Redis 缓存，回退 DB）
        Map<Long, String> saved = loadSavedAnswers(record.getUserId(), record.getId());

        List<Map<String, Object>> questions = new ArrayList<>();
        for (PaperQuestion pq : pqs) {
            Question q = qmap.get(pq.getQuestionId());
            if (q == null) continue;
            Map<String, Object> item = new HashMap<>();
            item.put("id", q.getId());
            item.put("type", q.getType());
            item.put("content", q.getContent());
            item.put("optionA", q.getOptionA());
            item.put("optionB", q.getOptionB());
            item.put("optionC", q.getOptionC());
            item.put("optionD", q.getOptionD());
            item.put("score", pq.getScore());
            item.put("sortOrder", pq.getSortOrder());
            item.put("savedAnswer", saved.get(q.getId()));
            questions.add(item);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("examRecordId", record.getId());
        result.put("paperId", paper.getId());
        result.put("paperTitle", paper.getTitle());
        result.put("duration", paper.getDuration());
        LocalDateTime deadline = record.getStartTime().plusMinutes(paper.getDuration());
        result.put("startTime", record.getStartTime());
        result.put("endTime", deadline);
        result.put("totalScore", paper.getTotalScore());
        result.put("questions", questions);
        return result;
    }

    /** 读取已保存答案：先查 Redis 进度缓存，回退 answer_record 表 */
    private Map<Long, String> loadSavedAnswers(Long userId, Long examRecordId) {
        Map<Long, String> map = new HashMap<>();
        String pkey = progressKey(userId, examRecordId);
        Map<Object, Object> cache = redis.opsForHash().entries(pkey);
        boolean cacheHit = cache != null && !cache.isEmpty();
        if (cacheHit) {
            for (Map.Entry<Object, Object> e : cache.entrySet()) {
                String k = String.valueOf(e.getKey());
                if (k.startsWith("q:")) {
                    map.put(Long.valueOf(k.substring(2)), String.valueOf(e.getValue()));
                }
            }
        } else {
            List<AnswerRecord> answers = answerRecordMapper.findByExamRecordId(examRecordId);
            for (AnswerRecord a : answers) {
                map.put(a.getQuestionId(), a.getUserAnswer());
            }
        }
        return map;
    }

    private List<Map<String, Object>> buildAnswerDetailList(List<AnswerRecord> answers, Map<Long, Question> qmap) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (AnswerRecord a : answers) {
            Question q = qmap.get(a.getQuestionId());
            Map<String, Object> item = new HashMap<>();
            item.put("id", a.getId());
            item.put("questionId", a.getQuestionId());
            item.put("userAnswer", a.getUserAnswer());
            item.put("isCorrect", a.getIsCorrect());
            item.put("score", a.getScore());
            item.put("type", q != null ? q.getType() : "");
            item.put("content", q != null ? q.getContent() : "已删除");
            item.put("correctAnswer", q != null ? q.getAnswer() : "");
            item.put("maxScore", a.getScore());
            list.add(item);
        }
        return list;
    }

    // ==================== 私有：批改规则 ====================

    private boolean gradeQuestion(Question q, String userAnswer) {
        if (userAnswer == null) userAnswer = "";
        String correct = q.getAnswer();
        if (correct == null) correct = "";
        return switch (q.getType()) {
            case "single_choice", "true_false" -> userAnswer.trim().equalsIgnoreCase(correct.trim());
            case "multi_choice" -> sortAnswer(userAnswer).equals(sortAnswer(correct));
            case "fill_blank" -> userAnswer.trim().equalsIgnoreCase(correct.trim());
            case "essay" -> !userAnswer.trim().isEmpty();
            default -> false;
        };
    }

    private String sortAnswer(String answer) {
        if (answer == null || answer.isBlank()) return "";
        String[] parts = answer.trim().toUpperCase().split(",");
        List<String> list = new ArrayList<>();
        for (String p : parts) {
            String t = p.trim();
            if (!t.isEmpty()) list.add(t);
        }
        list.sort(String::compareTo);
        return String.join(",", list);
    }

    private String progressKey(Long userId, Long examRecordId) {
        return RedisKey.EXAM_PROGRESS + userId + ":" + examRecordId;
    }

    /** 批改中间结果 */
    private static class GradedResult {
        int totalScore;
        int correctCount;
        LocalDateTime submitTime;
        List<Map<String, Object>> details;
    }
}
