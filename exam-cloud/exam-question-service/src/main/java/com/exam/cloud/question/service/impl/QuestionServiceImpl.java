package com.exam.cloud.question.service.impl;

import com.exam.cloud.common.entity.Question;
import com.exam.cloud.question.mapper.QuestionMapper;
import com.exam.cloud.question.service.QuestionService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class QuestionServiceImpl implements QuestionService {

    private final QuestionMapper questionMapper;

    public QuestionServiceImpl(QuestionMapper questionMapper) {
        this.questionMapper = questionMapper;
    }

    @Override
    @CacheEvict(value = "questionList", allEntries = true)
    public boolean add(Question question) {
        return questionMapper.insert(question) > 0;
    }

    @Override
    @CacheEvict(value = "questionList", allEntries = true)
    public boolean update(Question question) {
        return questionMapper.updateById(question) > 0;
    }

    @Override
    @CacheEvict(value = "questionList", allEntries = true)
    public boolean delete(Long id) {
        return questionMapper.deleteById(id) > 0;
    }

    @Override
    public Question getById(Long id) {
        return questionMapper.selectById(id);
    }

    @Override
    public List<Question> listByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return Collections.emptyList();
        return questionMapper.selectBatchIds(ids);
    }

    @Override
    @Cacheable(value = "questionList", key = "T(java.util.Objects).hash(#type, #category, #difficulty, #keyword)")
    public List<Question> list(String type, String category, String difficulty, String keyword) {
        return questionMapper.findByCondition(type, category, difficulty, keyword);
    }
}
