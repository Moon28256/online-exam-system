package com.exam.backend.service;

import com.exam.backend.entity.Question;

import java.util.List;

public interface QuestionService {
    boolean add(Question question);
    boolean update(Question question);
    boolean delete(Long id);
    Question getById(Long id);
    List<Question> list(String type, String category, String difficulty, String keyword);
}
