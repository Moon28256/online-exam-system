package com.exam.cloud.paper.service.impl;

import com.exam.cloud.common.api.Result;
import com.exam.cloud.common.api.client.QuestionClient;
import com.exam.cloud.common.dto.PaperCreateDTO;
import com.exam.cloud.common.entity.Paper;
import com.exam.cloud.common.entity.PaperQuestion;
import com.exam.cloud.common.entity.Question;
import com.exam.cloud.paper.mapper.PaperMapper;
import com.exam.cloud.paper.mapper.PaperQuestionMapper;
import com.exam.cloud.paper.service.PaperService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class PaperServiceImpl implements PaperService {

    private final PaperMapper paperMapper;
    private final PaperQuestionMapper paperQuestionMapper;
    private final QuestionClient questionClient;

    public PaperServiceImpl(PaperMapper paperMapper,
                            PaperQuestionMapper paperQuestionMapper,
                            QuestionClient questionClient) {
        this.paperMapper = paperMapper;
        this.paperQuestionMapper = paperQuestionMapper;
        this.questionClient = questionClient;
    }

    @Override
    @Transactional
    @CacheEvict(value = {"paperDetail", "paperQuestions"}, allEntries = true)
    public boolean create(PaperCreateDTO dto) {
        Paper paper = new Paper();
        paper.setTitle(dto.getTitle());
        paper.setDescription(dto.getDescription());
        paper.setDuration(dto.getDuration());
        paper.setCreatorId(dto.getCreatorId());
        paper.setStatus("draft");

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            if (dto.getStartTime() != null) paper.setStartTime(sdf.parse(dto.getStartTime()));
            if (dto.getEndTime() != null) paper.setEndTime(sdf.parse(dto.getEndTime()));
        } catch (Exception ignored) {
        }

        paper.setTotalScore(sumScore(dto));
        paperMapper.insert(paper);

        bindQuestions(paper.getId(), dto);
        return true;
    }

    @Override
    @Transactional
    @CacheEvict(value = {"paperDetail", "paperQuestions"}, allEntries = true)
    public boolean update(Long id, PaperCreateDTO dto) {
        Paper paper = paperMapper.selectById(id);
        if (paper == null) return false;

        paper.setTitle(dto.getTitle());
        paper.setDescription(dto.getDescription());
        paper.setDuration(dto.getDuration());

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            if (dto.getStartTime() != null) paper.setStartTime(sdf.parse(dto.getStartTime()));
            if (dto.getEndTime() != null) paper.setEndTime(sdf.parse(dto.getEndTime()));
        } catch (Exception ignored) {
        }

        paper.setTotalScore(sumScore(dto));
        paperMapper.updateById(paper);

        paperQuestionMapper.deleteByPaperId(id);
        bindQuestions(id, dto);
        return true;
    }

    @Override
    @CacheEvict(value = {"paperDetail", "paperQuestions"}, allEntries = true)
    public boolean unpublish(Long id) {
        Paper paper = paperMapper.selectById(id);
        if (paper == null || !"published".equals(paper.getStatus())) return false;
        paper.setStatus("draft");
        return paperMapper.updateById(paper) > 0;
    }

    @Override
    @Transactional
    @CacheEvict(value = {"paperDetail", "paperQuestions"}, allEntries = true)
    public boolean delete(Long id) {
        paperQuestionMapper.deleteByPaperId(id);
        return paperMapper.deleteById(id) > 0;
    }

    @Override
    @CacheEvict(value = {"paperDetail", "paperQuestions"}, allEntries = true)
    public boolean publish(Long id) {
        Paper paper = paperMapper.selectById(id);
        if (paper == null || !"draft".equals(paper.getStatus())) return false;
        paper.setStatus("published");
        return paperMapper.updateById(paper) > 0;
    }

    @Override
    @Cacheable(value = "paperDetail", key = "#id")
    public Paper getById(Long id) {
        return paperMapper.selectById(id);
    }

    @Override
    public List<Paper> list(String status) {
        if (status != null && !status.isEmpty()) {
            return paperMapper.findByStatus(status);
        }
        return paperMapper.findAll();
    }

    @Override
    @Cacheable(value = "paperQuestions", key = "#paperId")
    public List<Map<String, Object>> getQuestions(Long paperId) {
        List<PaperQuestion> pqs = paperQuestionMapper.findByPaperId(paperId);
        Map<Long, Question> qmap = fetchQuestions(pqs);
        List<Map<String, Object>> result = new ArrayList<>();
        for (PaperQuestion pq : pqs) {
            Question q = qmap.get(pq.getQuestionId());
            if (q != null) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", q.getId());
                map.put("type", q.getType());
                map.put("content", q.getContent());
                map.put("optionA", q.getOptionA());
                map.put("optionB", q.getOptionB());
                map.put("optionC", q.getOptionC());
                map.put("optionD", q.getOptionD());
                map.put("answer", q.getAnswer());
                map.put("score", pq.getScore());
                map.put("sortOrder", pq.getSortOrder());
                result.add(map);
            }
        }
        return result;
    }

    @Override
    @Cacheable(value = "paperQuestions", key = "'raw:' + #paperId")
    public List<PaperQuestion> getPaperQuestions(Long paperId) {
        return paperQuestionMapper.findByPaperId(paperId);
    }

    // ==================== 私有方法 ====================

    private int sumScore(PaperCreateDTO dto) {
        int total = 0;
        if (dto.getQuestions() != null) {
            for (PaperCreateDTO.QuestionItem item : dto.getQuestions()) {
                total += (item.getScore() != null ? item.getScore() : 5);
            }
        }
        return total;
    }

    private void bindQuestions(Long paperId, PaperCreateDTO dto) {
        if (dto.getQuestions() == null || dto.getQuestions().isEmpty()) return;
        int order = 1;
        for (PaperCreateDTO.QuestionItem item : dto.getQuestions()) {
            PaperQuestion pq = new PaperQuestion();
            pq.setPaperId(paperId);
            pq.setQuestionId(item.getQuestionId());
            pq.setScore(item.getScore() != null ? item.getScore() : 5);
            pq.setSortOrder(order++);
            paperQuestionMapper.insert(pq);
        }
    }

    /** 经 Feign 批量拉取题目（仅取需要的字段，减少跨服务开销） */
    private Map<Long, Question> fetchQuestions(List<PaperQuestion> pqs) {
        if (pqs == null || pqs.isEmpty()) return Collections.emptyMap();
        List<Long> ids = pqs.stream().map(PaperQuestion::getQuestionId).distinct().toList();
        try {
            Result<List<Question>> res = questionClient.batch(ids);
            if (res != null && res.getData() != null) {
                Map<Long, Question> map = new HashMap<>();
                for (Question q : res.getData()) {
                    map.put(q.getId(), q);
                }
                return map;
            }
        } catch (Exception e) {
            // 题目服务不可用时退化为空（保持试卷可见，题目缺失）
        }
        return Collections.emptyMap();
    }
}
