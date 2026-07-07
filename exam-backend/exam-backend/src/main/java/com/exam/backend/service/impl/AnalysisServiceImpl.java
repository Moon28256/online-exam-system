package com.exam.backend.service.impl;

import com.exam.backend.mapper.AnalysisMapper;
import com.exam.backend.mapper.ScoreMapper;
import com.exam.backend.service.AnalysisService;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class AnalysisServiceImpl implements AnalysisService {

    private final AnalysisMapper analysisMapper;
    private final ScoreMapper scoreMapper;

    public AnalysisServiceImpl(AnalysisMapper analysisMapper, ScoreMapper scoreMapper) {
        this.analysisMapper = analysisMapper;
        this.scoreMapper = scoreMapper;
    }

    @Override
    public Map<String, Object> getOverview() {
        List<Map<String, Object>> rows = analysisMapper.overview();
        Map<String, Object> result = new HashMap<>();
        for (Map<String, Object> row : rows) {
            result.put((String) row.get("name"), row.get("value"));
        }
        return result;
    }

    @Override
    public List<Map<String, Object>> getScoreDistribution(Long paperId) {
        return analysisMapper.scoreDistribution(paperId);
    }

    @Override
    public List<Map<String, Object>> getMostMissedQuestions() {
        return analysisMapper.mostMissedQuestions();
    }

    @Override
    public Map<String, Object> getPerformance() {
        Map<String, Object> result = new HashMap<>();
        result.put("byCategory", analysisMapper.categoryPerformance());
        result.put("byType", analysisMapper.typePerformance());
        return result;
    }

    @Override
    public List<Map<String, Object>> getStudentTrend(Long userId) {
        // 使用 score 表的统计数据（按试卷分组）
        return scoreMapper.statisticsByUser(userId);
    }

    @Override
    public List<Map<String, Object>> getRecentActivity() {
        return analysisMapper.recentActivity();
    }
}
