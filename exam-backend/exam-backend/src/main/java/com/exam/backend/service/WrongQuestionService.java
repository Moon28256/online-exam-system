package com.exam.backend.service;

import java.util.List;
import java.util.Map;

public interface WrongQuestionService {
    List<Map<String, Object>> getMyWrongQuestions(Long userId);
    List<Map<String, Object>> getStatistics(Long userId);
    boolean remove(Long userId, Long wrongQuestionId);
    boolean removeByQuestion(Long userId, Long questionId);
}
