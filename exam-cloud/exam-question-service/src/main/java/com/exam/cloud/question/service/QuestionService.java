package com.exam.cloud.question.service;

import com.exam.cloud.common.entity.Question;

import java.util.List;

public interface QuestionService {
    boolean add(Question question);
    boolean update(Question question);
    boolean delete(Long id);
    Question getById(Long id);
    List<Question> listByIds(List<Long> ids);
    List<Question> list(String type, String category, String difficulty, String keyword);
}
