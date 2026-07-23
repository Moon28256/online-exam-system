package com.exam.cloud.common.feign;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 统一 Feign 配置：注册鉴权拦截器。
 * 各 @FeignClient 通过 configuration = FeignClientConfig.class 引用。
 */
@Configuration
public class FeignClientConfig {

    @Bean
    public FeignAuthInterceptor feignAuthInterceptor() {
        return new FeignAuthInterceptor();
    }
}
