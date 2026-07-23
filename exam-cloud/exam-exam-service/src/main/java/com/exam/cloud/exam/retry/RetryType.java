package com.exam.cloud.exam.retry;

/** 重试任务类型 */
public enum RetryType {
    SCORE,  // 写入成绩快照
    WRONG   // 维护错题本
}
