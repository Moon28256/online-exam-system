package com.exam.cloud.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

// 仅扫描网关自身包与 common.auth（JwtUtil），避免加载 common 的 Servlet 过滤器/ControllerAdvice
@SpringBootApplication(scanBasePackages = {"com.exam.cloud.gateway", "com.exam.cloud.common.auth"})
@EnableDiscoveryClient
public class GatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }
}
