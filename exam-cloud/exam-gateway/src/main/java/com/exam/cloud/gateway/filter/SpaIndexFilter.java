package com.exam.cloud.gateway.filter;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * 托管前端单页应用 + /test 健康检查（WebFilter，运行于网关路由之前）。
 *
 * - API 前缀（/user /question /paper /exam /score /wrong /analysis）→ 放行
 * - /test → 健康检查 JSON
 * - 带后缀（.css .js .svg …）→ 交给 Spring 内置静态资源处理器
 * - 无后缀 → SPA 路由，重写为 /index.html 交给 Spring 处理
 */
@Component
public class SpaIndexFilter implements WebFilter {

    private static final List<String> API_PREFIX = List.of(
            "/user/", "/question/", "/paper/", "/exam/", "/score/", "/wrong/", "/analysis/");

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        if (!HttpMethod.GET.equals(request.getMethod())) {
            return chain.filter(exchange);
        }

        // API 请求放行交网关路由
        for (String p : API_PREFIX) {
            if (path.startsWith(p)) return chain.filter(exchange);
        }

        // 健康检查
        if (path.equals("/test")) {
            return writeBody(exchange, HttpStatus.OK, MediaType.APPLICATION_JSON,
                    "{\"code\":200,\"message\":\"success\",\"data\":\"后端启动成功！\"}");
        }

        // 带后缀的请求（.css .js .svg 等静态资源）→ 交给 Spring 内置静态资源处理器
        if (path.contains(".")) {
            return chain.filter(exchange);
        }

        // SPA 路由（无后缀）→ 重写路径为 /index.html，交给 Spring 静态资源处理器
        ServerHttpRequest mutated = exchange.getRequest().mutate()
                .path("/index.html")
                .build();
        return chain.filter(exchange.mutate().request(mutated).build());
    }

    private Mono<Void> writeBody(ServerWebExchange exchange, HttpStatus status, MediaType type, String body) {
        return writeBody(exchange, status, type, body.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    private Mono<Void> writeBody(ServerWebExchange exchange, HttpStatus status, MediaType type, byte[] bytes) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().setContentType(type);
        response.getHeaders().setContentLength(bytes.length);
        return response.writeWith(Mono.just(response.bufferFactory().wrap(bytes)));
    }
}
