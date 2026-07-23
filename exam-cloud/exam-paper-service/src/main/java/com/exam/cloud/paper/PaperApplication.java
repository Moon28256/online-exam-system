package com.exam.cloud.paper;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = {"com.exam.cloud.paper", "com.exam.cloud.common.web"})
@EnableDiscoveryClient
@EnableCaching
@EnableFeignClients(basePackages = "com.exam.cloud.common.api.client")
@MapperScan("com.exam.cloud.paper.mapper")
public class PaperApplication {
    public static void main(String[] args) {
        SpringApplication.run(PaperApplication.class, args);
    }
}
