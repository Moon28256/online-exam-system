package com.exam.cloud.gateway.filter;

import com.exam.cloud.common.auth.AuthHeaders;
import com.exam.cloud.common.auth.JwtUtil;
import com.exam.cloud.common.constant.RedisKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

/**
 * 全局鉴权过滤器（网关是唯一外部入口）：
 * 1. 白名单路径（登录/注册/测试/静态资源）放行
 * 2. 校验 JWT 签名与过期
 * 3. 校验 Redis 中 token 仍存在（支持登出/踢人）
 * 4. 将 userId/username/role 注入 X-User-* 头下发下游
 * 下游服务信任网关头，通过 UserHeaderFilter 转为 request 属性。
 */
@Component
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(AuthGlobalFilter.class);

    /** 白名单：无需登录即可访问 */
    private static final Set<String> WHITELIST = Set.of(
            "/user/login", "/user/register", "/test",
            "/", "/index.html", "/favicon.ico", "/favicon.svg", "/icons.svg", "/error"
    );
    private static final List<String> WHITELIST_PREFIX = List.of("/assets/");

    private final JwtUtil jwtUtil;
    private final ReactiveStringRedisTemplate redis;

    public AuthGlobalFilter(JwtUtil jwtUtil, ReactiveStringRedisTemplate redis) {
        this.jwtUtil = jwtUtil;
        this.redis = redis;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // OPTIONS 预检放行
        if ("OPTIONS".equalsIgnoreCase(request.getMethod().name())) {
            return chain.filter(exchange);
        }

        // 白名单放行
        if (isWhitelisted(path)) {
            return chain.filter(exchange);
        }

        // 解析 Token
        String auth = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (auth == null || auth.isEmpty()) {
            return unauthorized(exchange, "未登录，请先登录");
        }
        String token = auth.startsWith("Bearer ") ? auth.substring(7) : auth;

        if (!jwtUtil.validate(token)) {
            return unauthorized(exchange, "Token无效或已过期，请重新登录");
        }

        Long userId;
        String username;
        String role;
        try {
            userId = jwtUtil.getUserId(token);
            username = jwtUtil.getUsername(token);
            role = jwtUtil.getRole(token);
        } catch (Exception e) {
            return unauthorized(exchange, "Token解析失败，请重新登录");
        }

        // Redis 校验 token 仍存在（登出/踢人后即失效）
        String tokenKey = RedisKey.LOGIN_TOKEN + userId;
        return redis.opsForValue().get(tokenKey)
                .defaultIfEmpty("")
                .flatMap(stored -> {
                    if (stored == null || stored.isEmpty()) {
                        return unauthorized(exchange, "登录已失效，请重新登录");
                    }
                    // 注入用户身份头
                    ServerHttpRequest mutated = request.mutate()
                            .header(AuthHeaders.USER_ID, String.valueOf(userId))
                            .header(AuthHeaders.USERNAME, username == null ? "" : username)
                            .header(AuthHeaders.ROLE, role == null ? "" : role)
                            .build();
                    return chain.filter(exchange.mutate().request(mutated).build());
                })
                .onErrorResume(e -> {
                    // Redis 异常时放行（可用性优先，记录告警）
                    log.warn("Redis 鉴权异常，放行 userId={}: {}", userId, e.getMessage());
                    ServerHttpRequest mutated = request.mutate()
                            .header(AuthHeaders.USER_ID, String.valueOf(userId))
                            .header(AuthHeaders.USERNAME, username == null ? "" : username)
                            .header(AuthHeaders.ROLE, role == null ? "" : role)
                            .build();
                    return chain.filter(exchange.mutate().request(mutated).build());
                });
    }

    private boolean isWhitelisted(String path) {
        if (WHITELIST.contains(path)) return true;
        for (String p : WHITELIST_PREFIX) {
            if (path.startsWith(p)) return true;
        }
        return false;
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String msg) {
        String body = "{\"code\":401,\"message\":\"" + msg + "\",\"data\":null}";
        return writeJson(exchange, HttpStatus.UNAUTHORIZED, body);
    }

    private Mono<Void> writeJson(ServerWebExchange exchange, HttpStatus status, String body) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        response.getHeaders().setContentLength(body.getBytes(StandardCharsets.UTF_8).length);
        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        // 早于网关路由，先鉴权
        return -100;
    }
}
