package com.exam.backend.service.impl;

import com.exam.backend.dto.PaperCreateDTO;
import com.exam.backend.entity.Paper;
import com.exam.backend.entity.PaperQuestion;
import com.exam.backend.entity.Question;
import com.exam.backend.mapper.PaperMapper;
import com.exam.backend.mapper.PaperQuestionMapper;
import com.exam.backend.mapper.QuestionMapper;
import com.exam.backend.service.PaperService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class PaperServiceImpl implements PaperService {

    private final PaperMapper paperMapper;
    private final PaperQuestionMapper paperQuestionMapper;
    private final QuestionMapper questionMapper;

    public PaperServiceImpl(PaperMapper paperMapper,
                            PaperQuestionMapper paperQuestionMapper,
                            QuestionMapper questionMapper) {
        this.paperMapper = paperMapper;
        this.paperQuestionMapper = paperQuestionMapper;
        this.questionMapper = questionMapper;
    }

    @Override
    @Transactional
    public boolean create(PaperCreateDTO dto) {
        Paper paper = new Paper();
        paper.setTitle(dto.getTitle());
        paper.setDescription(dto.getDescription());
        paper.setDuration(dto.getDuration());
        paper.setCreatorId(dto.getCreatorId());
        paper.setStatus("draft");

        // 处理时间字段
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            if (dto.getStartTime() != null) paper.setStartTime(sdf.parse(dto.getStartTime()));
            if (dto.getEndTime() != null) paper.setEndTime(sdf.parse(dto.getEndTime()));
        } catch (Exception ignored) {}

        // 自动计算总分 = 每道题分数之和
        int totalScore = 0;
        if (dto.getQuestions() != null) {
            for (PaperCreateDTO.QuestionItem item : dto.getQuestions()) {
                totalScore += (item.getScore() != null ? item.getScore() : 5);
            }
        }
        paper.setTotalScore(totalScore);

        paperMapper.insert(paper);

        // 绑定题目
        if (dto.getQuestions() != null && !dto.getQuestions().isEmpty()) {
            int order = 1;
            for (PaperCreateDTO.QuestionItem item : dto.getQuestions()) {
                PaperQuestion pq = new PaperQuestion();
                pq.setPaperId(paper.getId());
                pq.setQuestionId(item.getQuestionId());
                pq.setScore(item.getScore() != null ? item.getScore() : 5);
                pq.setSortOrder(order++);
                paperQuestionMapper.insert(pq);
            }
        }
        return true;
    }

    @Override
    @Transactional
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
        } catch (Exception ignored) {}

        // 自动计算总分
        int totalScore = 0;
        if (dto.getQuestions() != null) {
            for (PaperCreateDTO.QuestionItem item : dto.getQuestions()) {
                totalScore += (item.getScore() != null ? item.getScore() : 5);
            }
        }
        paper.setTotalScore(totalScore);

        paperMapper.updateById(paper);

        // 重新绑定题目：先删旧关联，再插入新关联
        paperQuestionMapper.deleteByPaperId(id);
        if (dto.getQuestions() != null && !dto.getQuestions().isEmpty()) {
            int order = 1;
            for (PaperCreateDTO.QuestionItem item : dto.getQuestions()) {
                PaperQuestion pq = new PaperQuestion();
                pq.setPaperId(id);
                pq.setQuestionId(item.getQuestionId());
                pq.setScore(item.getScore() != null ? item.getScore() : 5);
                pq.setSortOrder(order++);
                paperQuestionMapper.insert(pq);
            }
        }
        return true;
    }

    @Override
    public boolean unpublish(Long id) {
        Paper paper = paperMapper.selectById(id);
        if (paper == null || !"published".equals(paper.getStatus())) return false;
        paper.setStatus("draft");
        return paperMapper.updateById(paper) > 0;
    }

    @Override
    @Transactional
    public boolean delete(Long id) {
        paperQuestionMapper.deleteByPaperId(id);
        return paperMapper.deleteById(id) > 0;
    }

    @Override
    public boolean publish(Long id) {
        Paper paper = paperMapper.selectById(id);
        if (paper == null || !"draft".equals(paper.getStatus())) return false;
        paper.setStatus("published");
        return paperMapper.updateById(paper) > 0;
    }

    @Override
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
    public List<Map<String, Object>> getQuestions(Long paperId) {
        List<PaperQuestion> pqs = paperQuestionMapper.findByPaperId(paperId);
        List<Map<String, Object>> result = new ArrayList<>();
        for (PaperQuestion pq : pqs) {
            Question q = questionMapper.selectById(pq.getQuestionId());
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
}
