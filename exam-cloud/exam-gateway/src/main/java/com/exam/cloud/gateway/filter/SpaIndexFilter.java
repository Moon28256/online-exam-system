package com.exam.cloud.gateway.filter;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
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

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 托管前端单页应用 + /test 健康检查（WebFilter，运行于网关路由之前）。
 *
 * 对 GET 请求：
 *  - API 前缀（/user /question /paper /exam /score /wrong /analysis）→ 放行，交网关路由
 *  - /test → 直接返回健康检查 JSON
 *  - 其它 → 优先返回 classpath:/static<路径> 的静态文件；找不到则回退 index.html（SPA）
 *
 * 这样前端构建产物（exam-frontend/dist）拷入 static/ 后即可由网关单端口提供。
 */
@Component
public class SpaIndexFilter implements WebFilter {

    private static final List<String> API_PREFIX = List.of(
            "/user/", "/question/", "/paper/", "/exam/", "/score/", "/wrong/", "/analysis/");

    private static final Map<String, MediaType> CONTENT_TYPES = new HashMap<>();
    static {
        CONTENT_TYPES.put(".html", MediaType.TEXT_HTML);
        CONTENT_TYPES.put(".js",   MediaType.valueOf("text/javascript"));
        CONTENT_TYPES.put(".css", MediaType.TEXT_CSS);
        CONTENT_TYPES.put(".svg", MediaType.IMAGE_SVG_XML);
        CONTENT_TYPES.put(".png", MediaType.IMAGE_PNG);
        CONTENT_TYPES.put(".jpg", MediaType.IMAGE_JPEG);
        CONTENT_TYPES.put(".ico", MediaType.IMAGE_X_ICON);
        CONTENT_TYPES.put(".json", MediaType.APPLICATION_JSON);
        CONTENT_TYPES.put(".woff", MediaType.valueOf("font/woff"));
        CONTENT_TYPES.put(".woff2", MediaType.valueOf("font/woff2"));
    }

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

        // 静态文件：classpath:/static<path>
        if (!path.equals("/") && !path.equals("/index.html")) {
            Resource file = new ClassPathResource("/static" + (path.startsWith("/") ? path : "/" + path));
            if (file.exists() && file.isReadable()) {
                return serve(exchange, file, contentTypeOf(path));
            }
        }

        // SPA 回退 index.html
        Resource index = new ClassPathResource("/static/index.html");
        if (index.exists() && index.isReadable()) {
            return serve(exchange, index, MediaType.TEXT_HTML);
        }
        return writeBody(exchange, HttpStatus.NOT_FOUND, MediaType.TEXT_PLAIN,
                "前端未构建：请先在 exam-frontend 执行 npm run build，并将 dist 拷贝至 exam-gateway/src/main/resources/static/");
    }

    private MediaType contentTypeOf(String path) {
        int dot = path.lastIndexOf('.');
        if (dot < 0) return MediaType.APPLICATION_OCTET_STREAM;
        MediaType mt = CONTENT_TYPES.get(path.substring(dot).toLowerCase());
        return mt != null ? mt : MediaType.APPLICATION_OCTET_STREAM;
    }

    private Mono<Void> serve(ServerWebExchange exchange, Resource res, MediaType type) {
        try (InputStream in = res.getInputStream()) {
            byte[] bytes = in.readAllBytes();
            return writeBody(exchange, HttpStatus.OK, type, bytes);
        } catch (Exception e) {
            exchange.getResponse().setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
            return exchange.getResponse().setComplete();
        }
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
