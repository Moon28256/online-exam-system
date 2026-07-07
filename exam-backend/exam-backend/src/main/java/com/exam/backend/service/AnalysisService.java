package com.exam.backend.service;

import java.util.List;
import java.util.Map;

public interface AnalysisService {
    /** 系统概览 */
    Map<String, Object> getOverview();

    /** 某试卷成绩分布 */
    List<Map<String, Object>> getScoreDistribution(Long paperId);

    /** 高频错题 Top10 */
    List<Map<String, Object>> getMostMissedQuestions();

    /** 各类别/题型正确率 */
    Map<String, Object> getPerformance();

    /** 学生个人成绩趋势 */
    List<Map<String, Object>> getStudentTrend(Long userId);

    /** 最近考试活动 */
    List<Map<String, Object>> getRecentActivity();
}
