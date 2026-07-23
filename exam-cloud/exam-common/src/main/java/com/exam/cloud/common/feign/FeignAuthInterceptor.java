package com.exam.cloud.common.feign;

import com.exam.cloud.common.auth.AuthHeaders;
import feign.RequestInterceptor;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Feign 调用时透传网关注入的 X-User-* 头，使被调服务的 UserHeaderFilter 仍能识别用户身份。
 * 无 HTTP 上下文（如定时重试任务调用）则不添加头——此时内部端点应从请求体取 userId。
 * 由 {@link FeignClientConfig} 以 @Bean 形式在每个 Feign 客户端子上下文创建。
 */
public class FeignAuthInterceptor implements RequestInterceptor {

    @Override
    public void apply(feign.RequestTemplate template) {
        RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
        if (!(attrs instanceof ServletRequestAttributes sra)) {
            return;
        }
        var req = sra.getRequest();
        copy(req, AuthHeaders.USER_ID, template);
        copy(req, AuthHeaders.USERNAME, template);
        copy(req, AuthHeaders.ROLE, template);
    }

    private void copy(jakarta.servlet.http.HttpServletRequest req, String name, feign.RequestTemplate template) {
        String v = req.getHeader(name);
        if (v != null && !v.isEmpty()) {
            template.header(name, v);
        }
    }
}
