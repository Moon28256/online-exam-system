package com.exam.cloud.analysis.service;

import java.util.List;
import java.util.Map;

public interface AnalysisService {
    Map<String, Object> getOverview();
    List<Map<String, Object>> getScoreDistribution(Long paperId);
    List<Map<String, Object>> getMostMissedQuestions();
    Map<String, Object> getPerformance();
    List<Map<String, Object>> getStudentTrend(Long userId);
    List<Map<String, Object>> getRecentActivity();
}
