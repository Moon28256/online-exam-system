package com.exam.cloud.common.constant;

/**
 * Redis Key 命名集中管理，避免各服务硬编码字符串冲突。
 */
public final class RedisKey {

    private RedisKey() {}

    // ===== Token / 会话 =====
    /** login:token:<userId> -> token 串；网关校验存在性以支持登出/踢人 */
    public static final String LOGIN_TOKEN = "login:token:";
    /** login:user:<userId> -> 用户 JSON */
    public static final String LOGIN_USER  = "login:user:";

    // ===== 业务热点缓存 =====
    public static final String CACHE_QUESTION_LIST = "cache:question:list:";
    public static final String CACHE_PAPER          = "cache:paper:";
    public static final String CACHE_PAPER_QUESTIONS= "cache:paper:questions:";
    public static final String CACHE_OVERVIEW       = "cache:analysis:overview";
    public static final String CACHE_MY_TREND       = "cache:analysis:my:trend:";

    // ===== 考试状态 =====
    /** exam:progress:<userId>:<examRecordId> -> Hash（已答题目 + 开始时间） */
    public static final String EXAM_PROGRESS = "exam:progress:";
    /** exam:submit:lock:<examRecordId> -> SETNX 防止并发重复交卷 */
    public static final String EXAM_SUBMIT_LOCK = "exam:submit:lock:";

    // ===== 排行榜 / 限流 =====
    /** rank:score:<paperId> -> ZSet（member=userId, score=分数） */
    public static final String RANK_SCORE = "rank:score:";
    /** rate:<scope>:<key> -> incr 计数（限流） */
    public static final String RATE_LOGIN = "rate:login:";
    public static final String RATE_SUBMIT= "rate:submit:";

    // ===== 幂等守卫（跨服务重试防重） =====
    public static final String GUARD_SCORE = "guard:score:exam:";
    public static final String GUARD_WRONG = "guard:wrong:exam:";

    // ===== 提交重试队列 =====
    /** retry:tasks -> ZSet（score=下次重试时间戳），payload 存 Hash */
    public static final String RETRY_ZSET  = "retry:tasks";
    public static final String RETRY_HASH  = "retry:task:";
}
