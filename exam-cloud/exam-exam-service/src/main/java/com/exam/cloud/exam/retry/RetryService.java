package com.exam.cloud.exam.retry;

import com.exam.cloud.common.api.Result;
import com.exam.cloud.common.api.client.ScoreClient;
import com.exam.cloud.common.api.client.WrongClient;
import com.exam.cloud.common.api.dto.ScoreCreateDTO;
import com.exam.cloud.common.api.dto.WrongBatchDTO;
import com.exam.cloud.common.constant.RedisKey;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * 基于 Redis 的重试队列：
 * - ZSet retry:tasks（score=下次重试时间戳）
 * - Hash retry:task:<id> 存 type/payload/attempts
 *
 * 考试提交后，向 score/wrong 服务的写入失败即入队，由 {@link RetryJob} 定时重放。
 * 被调端点（score/wrong）自身按 examRecordId 幂等，重试不会产生副作用。
 */
@Service
public class RetryService {

    private static final Logger log = LoggerFactory.getLogger(RetryService.class);
    private static final int MAX_ATTEMPTS = 5;

    private final StringRedisTemplate redis;
    private final ObjectMapper objectMapper;
    private final ScoreClient scoreClient;
    private final WrongClient wrongClient;

    public RetryService(StringRedisTemplate redis, ObjectMapper objectMapper,
                        ScoreClient scoreClient, WrongClient wrongClient) {
        this.redis = redis;
        this.objectMapper = objectMapper;
        this.scoreClient = scoreClient;
        this.wrongClient = wrongClient;
    }

    public void enqueueScore(ScoreCreateDTO dto) {
        enqueue(RetryType.SCORE, dto);
    }

    public void enqueueWrong(WrongBatchDTO dto) {
        enqueue(RetryType.WRONG, dto);
    }

    private void enqueue(RetryType type, Object payload) {
        try {
            String id = UUID.randomUUID().toString();
            Map<String, String> hash = new HashMap<>();
            hash.put("id", id);
            hash.put("type", type.name());
            hash.put("payload", objectMapper.writeValueAsString(payload));
            hash.put("attempts", "0");
            redis.opsForHash().putAll(RedisKey.RETRY_HASH + id, hash);
            // 立即可重试（score=当前时间戳）
            redis.opsForZSet().add(RedisKey.RETRY_ZSET, id, System.currentTimeMillis());
        } catch (Exception e) {
            log.error("入重试队列失败 type={}", type, e);
        }
    }

    /** 扫描到期任务并重放，由 RetryJob 调用 */
    public void scanAndReplay() {
        long now = System.currentTimeMillis();
        Set<String> due = redis.opsForZSet().rangeByScore(RedisKey.RETRY_ZSET, 0, now);
        if (due == null || due.isEmpty()) return;

        for (String id : due) {
            Map<Object, Object> hash = redis.opsForHash().entries(RedisKey.RETRY_HASH + id);
            if (hash == null || hash.isEmpty()) {
                redis.opsForZSet().remove(RedisKey.RETRY_ZSET, id);
                continue;
            }
            RetryTask task = new RetryTask();
            task.setId(id);
            task.setType((String) hash.get("type"));
            task.setPayload((String) hash.get("payload"));
            task.setAttempts(hash.get("attempts") == null ? 0 : Integer.parseInt((String) hash.get("attempts")));
            replay(task);
        }
    }

    private void replay(RetryTask task) {
        boolean ok;
        try {
            if (RetryType.SCORE.name().equals(task.getType())) {
                ScoreCreateDTO dto = objectMapper.readValue(task.getPayload(), ScoreCreateDTO.class);
                Result<Boolean> r = scoreClient.create(dto);
                ok = r != null && (r.getCode() == null || r.getCode() == 200);
            } else if (RetryType.WRONG.name().equals(task.getType())) {
                WrongBatchDTO dto = objectMapper.readValue(task.getPayload(), WrongBatchDTO.class);
                Result<Boolean> r = wrongClient.submitBatch(dto);
                ok = r != null && (r.getCode() == null || r.getCode() == 200);
            } else {
                ok = true; // 未知类型，丢弃
            }
        } catch (Exception e) {
            log.warn("重试失败 id={} type={} attempts={}: {}", task.getId(), task.getType(), task.getAttempts(), e.getMessage());
            ok = false;
        }

        if (ok) {
            redis.opsForZSet().remove(RedisKey.RETRY_ZSET, task.getId());
            redis.delete(RedisKey.RETRY_HASH + task.getId());
            log.info("重试任务完成 id={} type={}", task.getId(), task.getType());
            return;
        }

        int next = task.getAttempts() + 1;
        if (next >= MAX_ATTEMPTS) {
            // 超过上限：转入死信（删除队列项，保留 hash 供人工排查）
            redis.opsForZSet().remove(RedisKey.RETRY_ZSET, task.getId());
            redis.opsForHash().put(RedisKey.RETRY_HASH + task.getId(), "attempts", String.valueOf(next));
            log.error("重试任务超过最大次数，进入死信 id={} type={}", task.getId(), task.getType());
            return;
        }
        // 指数退避：30s, 60s, 120s, 240s ...
        long backoff = 30_000L * (1L << next);
        long nextTime = System.currentTimeMillis() + backoff;
        redis.opsForHash().put(RedisKey.RETRY_HASH + task.getId(), "attempts", String.valueOf(next));
        redis.opsForZSet().add(RedisKey.RETRY_ZSET, task.getId(), nextTime);
    }
}
