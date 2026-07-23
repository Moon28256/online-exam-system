package com.exam.cloud.common.web;

import com.exam.cloud.common.auth.AuthHeaders;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 把网关注入的 X-User-* 头转换为 request attribute（userId/username/role），
 * 使旧版 Controller 的 @RequestAttribute("userId") 无需改动即可工作。
 *
 * 仅在 Servlet（webmvc）服务生效；gateway 为反应式栈不扫描此包。
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class UserHeaderFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String userId = request.getHeader(AuthHeaders.USER_ID);
        if (userId != null && !userId.isEmpty()) {
            request.setAttribute(AuthHeaders.ATTR_USER_ID, Long.valueOf(userId));
            request.setAttribute(AuthHeaders.ATTR_USERNAME, request.getHeader(AuthHeaders.USERNAME));
            request.setAttribute(AuthHeaders.ATTR_ROLE, request.getHeader(AuthHeaders.ROLE));
        }
        chain.doFilter(request, response);
    }
}
