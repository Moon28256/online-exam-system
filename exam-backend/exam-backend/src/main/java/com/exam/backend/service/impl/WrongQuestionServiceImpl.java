package com.exam.backend.service.impl;

import com.exam.backend.entity.WrongQuestion;
import com.exam.backend.mapper.WrongQuestionMapper;
import com.exam.backend.service.WrongQuestionService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class WrongQuestionServiceImpl implements WrongQuestionService {

    private final WrongQuestionMapper wrongQuestionMapper;

    public WrongQuestionServiceImpl(WrongQuestionMapper wrongQuestionMapper) {
        this.wrongQuestionMapper = wrongQuestionMapper;
    }

    @Override
    public List<Map<String, Object>> getMyWrongQuestions(Long userId) {
        return wrongQuestionMapper.findMyWrongQuestions(userId);
    }

    @Override
    public List<Map<String, Object>> getStatistics(Long userId) {
        return wrongQuestionMapper.statisticsByUser(userId);
    }

    @Override
    public boolean remove(Long userId, Long id) {
        WrongQuestion wq = wrongQuestionMapper.selectById(id);
        if (wq == null || !wq.getUserId().equals(userId)) return false;
        return wrongQuestionMapper.deleteById(id) > 0;
    }

    @Override
    public boolean removeByQuestion(Long userId, Long questionId) {
        return wrongQuestionMapper.deleteByUserAndQuestion(userId, questionId) > 0;
    }
}
