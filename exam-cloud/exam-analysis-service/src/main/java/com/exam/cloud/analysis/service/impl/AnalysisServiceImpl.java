package com.exam.cloud.analysis.service.impl;

import com.exam.cloud.analysis.mapper.AnalysisMapper;
import com.exam.cloud.analysis.service.AnalysisService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class AnalysisServiceImpl implements AnalysisService {

    private final AnalysisMapper analysisMapper;

    public AnalysisServiceImpl(AnalysisMapper analysisMapper) {
        this.analysisMapper = analysisMapper;
    }

    @Override
    @Cacheable(value = "analysisOverview", unless = "false")
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
    @Cacheable(value = "analysisMostMissed", unless = "false")
    public List<Map<String, Object>> getMostMissedQuestions() {
        return analysisMapper.mostMissedQuestions();
    }

    @Override
    @Cacheable(value = "analysisPerformance", unless = "false")
    public Map<String, Object> getPerformance() {
        Map<String, Object> result = new HashMap<>();
        result.put("byCategory", analysisMapper.categoryPerformance());
        result.put("byType", analysisMapper.typePerformance());
        return result;
    }

    @Override
    @Cacheable(value = "analysisMyTrend", key = "#userId")
    public List<Map<String, Object>> getStudentTrend(Long userId) {
        return analysisMapper.studentTrend(userId);
    }

    @Override
    @Cacheable(value = "analysisRecentActivity", unless = "false")
    public List<Map<String, Object>> getRecentActivity() {
        return analysisMapper.recentActivity();
    }
}
