package com.exam.cloud.score.service.impl;

import com.exam.cloud.common.api.dto.ScoreCreateDTO;
import com.exam.cloud.common.constant.RedisKey;
import com.exam.cloud.common.entity.Score;
import com.exam.cloud.score.mapper.ScoreMapper;
import com.exam.cloud.score.service.ScoreService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Service
public class ScoreServiceImpl implements ScoreService {

    private final ScoreMapper scoreMapper;
    private final StringRedisTemplate redis;

    public ScoreServiceImpl(ScoreMapper scoreMapper, StringRedisTemplate redis) {
        this.scoreMapper = scoreMapper;
        this.redis = redis;
    }

    @Override
    public List<Map<String, Object>> getMyScores(Long userId) {
        return scoreMapper.statisticsByUser(userId);
    }

    @Override
    @Cacheable(value = "paperRanking", key = "#paperId")
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

    /**
     * 幂等写入：以 guard:score:exam:<examRecordId> 标记防重。
     * 同一 examRecordId 重复调用不会产生多条成绩。
     * 成功后同步更新排行榜 ZSet 并清除排名缓存。
     */
    @Override
    @Transactional
    @CacheEvict(value = "paperRanking", key = "#dto.paperId")
    public boolean createScore(ScoreCreateDTO dto) {
        String guard = RedisKey.GUARD_SCORE + dto.getExamRecordId();
        Boolean first = redis.opsForValue().setIfAbsent(guard, "1", Duration.ofDays(7));
        if (first != null && !first) {
            // 已处理过，幂等返回成功
            return true;
        }

        // 先清旧（若存在）再插入，避免 (user,paper) 重复
        scoreMapper.deleteByUserAndPaper(dto.getUserId(), dto.getPaperId());

        Score score = new Score();
        score.setUserId(dto.getUserId());
        score.setPaperId(dto.getPaperId());
        score.setScore(dto.getScore());
        score.setSubmitTime(dto.getSubmitTime());
        scoreMapper.insert(score);

        // 维护实时排行榜 ZSet
        redis.opsForZSet().add(RedisKey.RANK_SCORE + dto.getPaperId(),
                String.valueOf(dto.getUserId()), dto.getScore());
        return true;
    }
}
