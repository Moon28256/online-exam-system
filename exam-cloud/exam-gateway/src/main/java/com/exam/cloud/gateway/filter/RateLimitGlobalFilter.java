package com.exam.cloud.gateway.filter;

import com.exam.cloud.common.auth.AuthHeaders;
import com.exam.cloud.common.constant.RedisKey;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * 简单 Redis 限流：登录按 IP、交卷按用户，每分钟 N 次。
 * 用 INCR + 首次 EXPIRE 实现 60s 滑动窗口（最佳努力）。
 */
@Component
public class RateLimitGlobalFilter implements GlobalFilter, Ordered {

    private static final int LOGIN_LIMIT = 10;   // 每分钟 10 次
    private static final int SUBMIT_LIMIT = 10;

    private final ReactiveStringRedisTemplate redis;

    public RateLimitGlobalFilter(ReactiveStringRedisTemplate redis) {
        this.redis = redis;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        String key;
        int limit;

        if (path.equals("/user/login")) {
            key = RedisKey.RATE_LOGIN + clientIp(exchange.getRequest());
            limit = LOGIN_LIMIT;
        } else if (path.startsWith("/exam/submit")) {
            String uid = exchange.getRequest().getHeaders().getFirst(AuthHeaders.USER_ID);
            if (uid == null || uid.isEmpty()) {
                // 未鉴权（不应到达，Auth 在前），放行交给后续处理
                return chain.filter(exchange);
            }
            key = RedisKey.RATE_SUBMIT + uid;
            limit = SUBMIT_LIMIT;
        } else {
            return chain.filter(exchange);
        }

        final String fkey = key;
        final int flimit = limit;
        return redis.opsForValue().increment(fkey)
                .flatMap(count -> {
                    Mono<Void> after;
                    if (count != null && count == 1L) {
                        after = redis.expire(fkey, Duration.ofMinutes(1)).then();
                    } else {
                        after = Mono.empty();
                    }
                    if (count != null && count > flimit) {
                        return after.then(tooMany(exchange));
                    }
                    return after.then(chain.filter(exchange));
                });
    }

    private String clientIp(ServerHttpRequest request) {
        String ip = request.getHeaders().getFirst("X-Forwarded-For");
        if (ip != null && !ip.isEmpty()) {
            int comma = ip.indexOf(',');
            return comma > 0 ? ip.substring(0, comma).trim() : ip.trim();
        }
        return request.getRemoteAddress() != null
                ? request.getRemoteAddress().getAddress().getHostAddress()
                : "unknown";
    }

    private Mono<Void> tooMany(ServerWebExchange exchange) {
        String body = "{\"code\":429,\"message\":\"请求过于频繁，请稍后再试\",\"data\":null}";
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        response.getHeaders().setContentLength(body.getBytes(StandardCharsets.UTF_8).length);
        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return -90;  // 在 AuthGlobalFilter 之后
    }
}
