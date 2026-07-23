package com.exam.cloud.exam.retry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 定时扫描 Redis 重试队列，重放失败的成绩/错题写入。
 */
@Component
public class RetryJob {

    private static final Logger log = LoggerFactory.getLogger(RetryJob.class);

    private final RetryService retryService;

    public RetryJob(RetryService retryService) {
        this.retryService = retryService;
    }

    @Scheduled(fixedDelay = 30_000, initialDelay = 30_000)
    public void run() {
        try {
            retryService.scanAndReplay();
        } catch (Exception e) {
            log.warn("重试扫描异常: {}", e.getMessage());
        }
    }
}
