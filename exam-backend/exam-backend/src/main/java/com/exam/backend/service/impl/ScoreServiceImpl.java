package com.exam.backend.service.impl;

import com.exam.backend.mapper.ScoreMapper;
import com.exam.backend.service.ScoreService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ScoreServiceImpl implements ScoreService {

    private final ScoreMapper scoreMapper;

    public ScoreServiceImpl(ScoreMapper scoreMapper) {
        this.scoreMapper = scoreMapper;
    }

    @Override
    public List<Map<String, Object>> getMyScores(Long userId) {
        return scoreMapper.statisticsByUser(userId);
    }

    @Override
    public List<Map<String, Object>> getPaperRanking(Long paperId) {
        return scoreMapper.findRankingByPaperId(paperId);
    }

    @Override
    public Map<String, Object> getMyStatistics(Long userId) {
        List<Map<String, Object>> list = scoreMapper.statisticsByUser(userId);
        int totalExams = list.size();
        double overallAvg = list.stream()
                .mapToDouble(m -> ((Number) m.getOrDefault("avg_score", 0)).doubleValue())
                .average().orElse(0);
        int totalScore = list.stream()
                .mapToInt(m -> ((Number) m.getOrDefault("avg_score", 0)).intValue())
                .sum();

        return Map.of(
                "totalExams", totalExams,
                "overallAvg", Math.round(overallAvg * 10) / 10.0,
                "totalScore", totalScore,
                "details", list
        );
    }

    @Override
    public List<Map<String, Object>> getAllStatistics() {
        return scoreMapper.statisticsAll();
    }
}
