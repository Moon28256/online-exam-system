package com.exam.cloud.common.auth;

/**
 * 网关注入下游服务的用户身份 HTTP 头名称。
 * 下游服务信任网关头（网关是唯一外部入口）。
 */
public final class AuthHeaders {

    private AuthHeaders() {}

    public static final String USER_ID   = "X-User-Id";
    public static final String USERNAME  = "X-Username";
    public static final String ROLE       = "X-Role";

    /** 下游可用作 request attribute 的键名，保持与旧版 @RequestAttribute("userId") 兼容 */
    public static final String ATTR_USER_ID  = "userId";
    public static final String ATTR_USERNAME = "username";
    public static final String ATTR_ROLE     = "role";
}
