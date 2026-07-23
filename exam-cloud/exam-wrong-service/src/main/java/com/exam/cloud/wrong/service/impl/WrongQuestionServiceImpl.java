package com.exam.cloud.wrong.service.impl;

import com.exam.cloud.common.api.dto.WrongBatchDTO;
import com.exam.cloud.common.constant.RedisKey;
import com.exam.cloud.common.entity.WrongQuestion;
import com.exam.cloud.wrong.mapper.WrongQuestionMapper;
import com.exam.cloud.wrong.service.WrongQuestionService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class WrongQuestionServiceImpl implements WrongQuestionService {

    private final WrongQuestionMapper wrongQuestionMapper;
    private final StringRedisTemplate redis;

    public WrongQuestionServiceImpl(WrongQuestionMapper wrongQuestionMapper, StringRedisTemplate redis) {
        this.wrongQuestionMapper = wrongQuestionMapper;
        this.redis = redis;
    }

    @Override
    public List<Map<String, Object>> getMyWrongQuestions(Long userId) {
        return wrongQuestionMapper.findMyWrongQuestions(userId);
    }

    @Override
    public List<Map<String, Object>> getStatistics(Long userId) {
        return wrongQuestionMapper.statisticsByUser(userId);
    }

    @Override
    public boolean remove(Long userId, Long id) {
        WrongQuestion wq = wrongQuestionMapper.selectById(id);
        if (wq == null || !wq.getUserId().equals(userId)) return false;
        return wrongQuestionMapper.deleteById(id) > 0;
    }

    @Override
    public boolean removeByQuestion(Long userId, Long questionId) {
        return wrongQuestionMapper.deleteByUserAndQuestion(userId, questionId) > 0;
    }

    /**
     * 幂等维护：以 guard:wrong:exam:<examRecordId> 标记防重。
     * - 答对：从错题本移除
     * - 答错：已存在则 wrong_count+1，否则新增
     */
    @Override
    @Transactional
    public boolean submitBatch(WrongBatchDTO dto) {
        String guard = RedisKey.GUARD_WRONG + dto.getExamRecordId();
        Boolean first = redis.opsForValue().setIfAbsent(guard, "1", Duration.ofDays(7));
        if (first != null && !first) {
            return true; // 已处理
        }

        if (dto.getItems() == null) return true;
        for (WrongBatchDTO.Item item : dto.getItems()) {
            if (item.getIsCorrect() != null && item.getIsCorrect()) {
                wrongQuestionMapper.deleteByUserAndQuestion(dto.getUserId(), item.getQuestionId());
            } else {
                WrongQuestion wq = wrongQuestionMapper.findByUserAndQuestion(
                        dto.getUserId(), item.getQuestionId());
                if (wq != null) {
                    wq.setWrongCount(wq.getWrongCount() + 1);
                    wq.setLastWrongTime(LocalDateTime.now());
                    wrongQuestionMapper.updateById(wq);
                } else {
                    wq = new WrongQuestion();
                    wq.setUserId(dto.getUserId());
                    wq.setQuestionId(item.getQuestionId());
                    wq.setWrongCount(1);
                    wq.setLastWrongTime(LocalDateTime.now());
                    wrongQuestionMapper.insert(wq);
                }
            }
        }
        return true;
    }
}
