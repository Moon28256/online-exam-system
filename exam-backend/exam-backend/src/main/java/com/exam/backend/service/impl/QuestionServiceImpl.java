package com.exam.backend.service.impl;

import com.exam.backend.entity.Question;
import com.exam.backend.mapper.QuestionMapper;
import com.exam.backend.service.QuestionService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QuestionServiceImpl implements QuestionService {

    private final QuestionMapper questionMapper;

    public QuestionServiceImpl(QuestionMapper questionMapper) {
        this.questionMapper = questionMapper;
    }

    @Override
    public boolean add(Question question) {
        return questionMapper.insert(question) > 0;
    }

    @Override
    public boolean update(Question question) {
        return questionMapper.updateById(question) > 0;
    }

    @Override
    public boolean delete(Long id) {
        return questionMapper.deleteById(id) > 0;
    }

    @Override
    public Question getById(Long id) {
        return questionMapper.selectById(id);
    }

    @Override
    public List<Question> list(String type, String category, String difficulty, String keyword) {
        return questionMapper.findByCondition(type, category, difficulty, keyword);
    }
}
