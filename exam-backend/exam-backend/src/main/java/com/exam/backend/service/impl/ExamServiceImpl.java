package com.exam.backend.service.impl;

import com.exam.backend.entity.*;
import com.exam.backend.mapper.*;
import com.exam.backend.service.ExamService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class ExamServiceImpl implements ExamService {

    private final ExamRecordMapper examRecordMapper;
    private final AnswerRecordMapper answerRecordMapper;
    private final PaperMapper paperMapper;
    private final PaperQuestionMapper paperQuestionMapper;
    private final QuestionMapper questionMapper;
    private final ScoreMapper scoreMapper;
    private final WrongQuestionMapper wrongQuestionMapper;

    public ExamServiceImpl(ExamRecordMapper examRecordMapper,
                           AnswerRecordMapper answerRecordMapper,
                           PaperMapper paperMapper,
                           PaperQuestionMapper paperQuestionMapper,
                           QuestionMapper questionMapper,
                           ScoreMapper scoreMapper,
                           WrongQuestionMapper wrongQuestionMapper) {
        this.examRecordMapper = examRecordMapper;
        this.answerRecordMapper = answerRecordMapper;
        this.paperMapper = paperMapper;
        this.paperQuestionMapper = paperQuestionMapper;
        this.questionMapper = questionMapper;
        this.scoreMapper = scoreMapper;
        this.wrongQuestionMapper = wrongQuestionMapper;
    }

    @Override
    @Transactional
    public Map<String, Object> startExam(Long userId, Long paperId) {
        // 1. 校验试卷
        Paper paper = paperMapper.selectById(paperId);
        if (paper == null) throw new RuntimeException("试卷不存在");
        if (!"published".equals(paper.getStatus())) throw new RuntimeException("试卷未发布，无法考试");

        // 2. 检查是否有进行中的考试（同一试卷）
        ExamRecord inProgress = examRecordMapper.findInProgress(userId, paperId);
        if (inProgress != null) {
            // 已有进行中的记录，直接返回（断点续考）
            Map<String, Object> result = buildExamData(inProgress, paper);
            result.put("resume", true);
            return result;
        }

        // 3. 创建考试记录
        ExamRecord record = new ExamRecord();
        record.setUserId(userId);
        record.setPaperId(paperId);
        record.setStatus("in_progress");
        record.setStartTime(LocalDateTime.now());
        examRecordMapper.insert(record);

        Map<String, Object> result = buildExamData(record, paper);
        result.put("resume", false);
        return result;
    }

    @Override
    public boolean submitAnswer(Long userId, Long examRecordId, Long questionId, String userAnswer) {
        // 校验考试记录存在且属于当前用户
        ExamRecord exam = examRecordMapper.selectById(examRecordId);
        if (exam == null) throw new RuntimeException("考试记录不存在");
        if (!exam.getUserId().equals(userId)) throw new RuntimeException("无权操作");
        if (!"in_progress".equals(exam.getStatus())) throw new RuntimeException("考试已结束");

        // 校验题目属于该试卷
        List<PaperQuestion> pqs = paperQuestionMapper.findByPaperId(exam.getPaperId());
        boolean valid = pqs.stream().anyMatch(pq -> pq.getQuestionId().equals(questionId));
        if (!valid) throw new RuntimeException("该题目不属于本试卷");

        // 查找已有答案，有则更新，无则新增
        AnswerRecord answer = answerRecordMapper.findOne(examRecordId, questionId);
        if (answer != null) {
            answer.setUserAnswer(userAnswer);
            answerRecordMapper.updateById(answer);
        } else {
            answer = new AnswerRecord();
            answer.setExamRecordId(examRecordId);
            answer.setQuestionId(questionId);
            answer.setUserAnswer(userAnswer);
            answer.setIsCorrect(0); // 先置0，交卷时批改
            answer.setScore(0);
            answerRecordMapper.insert(answer);
        }
        return true;
    }

    @Override
    @Transactional
    public Map<String, Object> submitExam(Long userId, Long examRecordId) {
        // 1. 校验
        ExamRecord exam = examRecordMapper.selectById(examRecordId);
        if (exam == null) throw new RuntimeException("考试记录不存在");
        if (!exam.getUserId().equals(userId)) throw new RuntimeException("无权操作");
        if (!"in_progress".equals(exam.getStatus())) throw new RuntimeException("考试已提交过");

        // 2. 获取试卷题目
        List<PaperQuestion> pqs = paperQuestionMapper.findByPaperId(exam.getPaperId());

        // 3. 批改每道题
        int correctCount = 0;
        int totalScore = 0;
        List<Map<String, Object>> gradedAnswers = new ArrayList<>();

        for (PaperQuestion pq : pqs) {
            Question q = questionMapper.selectById(pq.getQuestionId());
            AnswerRecord answer = answerRecordMapper.findOne(examRecordId, pq.getQuestionId());
            String userAnswer = (answer != null) ? answer.getUserAnswer() : "";

            boolean correct = gradeQuestion(q, userAnswer);
            int questionScore = correct ? pq.getScore() : 0;

            // 更新 answer_record
            if (answer != null) {
                answer.setIsCorrect(correct ? 1 : 0);
                answer.setScore(questionScore);
                answerRecordMapper.updateById(answer);
            } else {
                // 未作答，插入空答案记录
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

            // 错题本：做错则收录/累加，做对则移出
            if (correct) {
                wrongQuestionMapper.deleteByUserAndQuestion(userId, pq.getQuestionId());
            } else {
                WrongQuestion wq = wrongQuestionMapper.findByUserAndQuestion(userId, pq.getQuestionId());
                if (wq != null) {
                    wq.setWrongCount(wq.getWrongCount() + 1);
                    wq.setLastWrongTime(LocalDateTime.now());
                    wrongQuestionMapper.updateById(wq);
                } else {
                    wq = new WrongQuestion();
                    wq.setUserId(userId);
                    wq.setQuestionId(pq.getQuestionId());
                    wq.setWrongCount(1);
                    wq.setLastWrongTime(LocalDateTime.now());
                    wrongQuestionMapper.insert(wq);
                }
            }

            Map<String, Object> detail = new HashMap<>();
            detail.put("questionId", q.getId());
            detail.put("type", q.getType());
            detail.put("content", q.getContent());
            detail.put("optionA", q.getOptionA());
            detail.put("optionB", q.getOptionB());
            detail.put("optionC", q.getOptionC());
            detail.put("optionD", q.getOptionD());
            detail.put("userAnswer", userAnswer);
            detail.put("correctAnswer", q.getAnswer());
            detail.put("isCorrect", correct);
            detail.put("score", questionScore);
            detail.put("maxScore", pq.getScore());
            gradedAnswers.add(detail);
        }

        // 4. 更新考试记录
        exam.setStatus("submitted");
        exam.setSubmitTime(LocalDateTime.now());
        exam.setTotalScore(totalScore);
        examRecordMapper.updateById(exam);

        // 5. 写入成绩表（成绩快照）
        Score score = new Score();
        score.setUserId(exam.getUserId());
        score.setPaperId(exam.getPaperId());
        score.setScore(totalScore);
        score.setSubmitTime(exam.getSubmitTime());
        scoreMapper.insert(score);

        // 6. 返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("examRecordId", exam.getId());
        result.put("totalScore", totalScore);
        result.put("maxScore", exam.getPaperId());  // 前端通过试卷详情获取
        result.put("correctCount", correctCount);
        result.put("totalCount", pqs.size());
        result.put("answers", gradedAnswers);
        return result;
    }

    @Override
    public Map<String, Object> getExamDetail(Long examRecordId) {
        ExamRecord exam = examRecordMapper.selectById(examRecordId);
        if (exam == null) throw new RuntimeException("考试记录不存在");

        Paper paper = paperMapper.selectById(exam.getPaperId());
        List<AnswerRecord> answers = answerRecordMapper.findByExamRecordId(examRecordId);

        Map<String, Object> result = new HashMap<>();
        result.put("exam", exam);
        result.put("paperTitle", paper != null ? paper.getTitle() : "已删除");
        result.put("answers", buildAnswerDetailList(answers));
        return result;
    }

    @Override
    public List<Map<String, Object>> getMyExamList(Long userId) {
        List<ExamRecord> records = examRecordMapper.findByUserId(userId);
        List<Map<String, Object>> result = new ArrayList<>();
        for (ExamRecord r : records) {
            Paper p = paperMapper.selectById(r.getPaperId());
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

    // ==================== 私有方法 ====================

    /** 构建考试数据（开始时返回的题目不含答案） */
    private Map<String, Object> buildExamData(ExamRecord record, Paper paper) {
        List<PaperQuestion> pqs = paperQuestionMapper.findByPaperId(paper.getId());
        List<Map<String, Object>> questions = new ArrayList<>();

        // 获取已保存的答案（断点续考用）
        List<AnswerRecord> savedAnswers = answerRecordMapper.findByExamRecordId(record.getId());

        for (PaperQuestion pq : pqs) {
            Question q = questionMapper.selectById(pq.getQuestionId());
            if (q == null) continue;
            Map<String, Object> item = new HashMap<>();
            item.put("id", q.getId());
            item.put("type", q.getType());
            item.put("content", q.getContent());
            item.put("optionA", q.getOptionA());
            item.put("optionB", q.getOptionB());
            item.put("optionC", q.getOptionC());
            item.put("optionD", q.getOptionD());
            item.put("score", pq.getScore());               // 该题在试卷中的分值
            item.put("sortOrder", pq.getSortOrder());

            // 如果有已保存答案，带回去给前端
            String saved = savedAnswers.stream()
                    .filter(a -> a.getQuestionId().equals(q.getId()))
                    .findFirst()
                    .map(AnswerRecord::getUserAnswer)
                    .orElse(null);
            item.put("savedAnswer", saved);

            questions.add(item);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("examRecordId", record.getId());
        result.put("paperId", paper.getId());
        result.put("paperTitle", paper.getTitle());
        result.put("duration", paper.getDuration());        // 考试时长（分钟）

        // 截止时间 = 开始时间 + 时长（分钟）
        LocalDateTime deadline = record.getStartTime().plusMinutes(paper.getDuration());
        result.put("startTime", record.getStartTime());
        result.put("endTime", deadline);                    // 个人截止时间，前端用于倒计时
        result.put("totalScore", paper.getTotalScore());
        result.put("questions", questions);
        return result;
    }

    /** 批改单道题 */
    private boolean gradeQuestion(Question q, String userAnswer) {
        if (userAnswer == null) userAnswer = "";
        String correct = q.getAnswer();
        if (correct == null) correct = "";

        return switch (q.getType()) {
            case "single_choice", "true_false" ->
                    userAnswer.trim().equalsIgnoreCase(correct.trim());

            case "multi_choice" ->
                    sortAnswer(userAnswer).equals(sortAnswer(correct));

            case "fill_blank" ->
                    userAnswer.trim().equalsIgnoreCase(correct.trim());

            case "essay" ->
                    // 主观题：有作答就给分（简化处理，实际应人工批改），给一半分
                    !userAnswer.trim().isEmpty();

            default -> false;
        };
    }

    /** 多选题答案排序：把 "A,C,B" → "A,B,C" */
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

    private List<Map<String, Object>> buildAnswerDetailList(List<AnswerRecord> answers) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (AnswerRecord a : answers) {
            Question q = questionMapper.selectById(a.getQuestionId());
            Map<String, Object> item = new HashMap<>();
            item.put("id", a.getId());
            item.put("questionId", a.getQuestionId());
            item.put("userAnswer", a.getUserAnswer());
            item.put("isCorrect", a.getIsCorrect());
            item.put("score", a.getScore());
            item.put("type", q != null ? q.getType() : "");
            item.put("content", q != null ? q.getContent() : "已删除");
            item.put("correctAnswer", q != null ? q.getAnswer() : "");
            item.put("maxScore", a.getScore());  // answer_record的score就是提交时的分值
            list.add(item);
        }
        return list;
    }
}
