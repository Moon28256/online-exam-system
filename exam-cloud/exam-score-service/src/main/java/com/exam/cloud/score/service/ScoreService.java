package com.exam.cloud.score.service;

import com.exam.cloud.common.api.dto.ScoreCreateDTO;

import java.util.List;
import java.util.Map;

public interface ScoreService {
    List<Map<String, Object>> getMyScores(Long userId);
    List<Map<String, Object>> getPaperRanking(Long paperId);
    Map<String, Object> getMyStatistics(Long userId);
    List<Map<String, Object>> getAllStatistics();

    /** 内部：考试提交后写入成绩快照（按 examRecordId 幂等） */
    boolean createScore(ScoreCreateDTO dto);
}
