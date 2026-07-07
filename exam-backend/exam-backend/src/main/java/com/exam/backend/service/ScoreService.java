package com.exam.backend.service;

import java.util.List;
import java.util.Map;

public interface ScoreService {
    List<Map<String, Object>> getMyScores(Long userId);
    List<Map<String, Object>> getPaperRanking(Long paperId);
    Map<String, Object> getMyStatistics(Long userId);
    List<Map<String, Object>> getAllStatistics();
}
