package com.exam.cloud.exam.retry;

import lombok.Data;

/**
 * 重试任务载体（序列化进 Redis Hash）。
 * type 对应 {@link RetryType}；payload 为 JSON 串；attempts 为已重试次数。
 */
@Data
public class RetryTask {
    private String id;
    private String type;
    private String payload;
    private int attempts;
}
