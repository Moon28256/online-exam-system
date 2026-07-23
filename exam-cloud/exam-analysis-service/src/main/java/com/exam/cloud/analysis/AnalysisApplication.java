package com.exam.cloud.analysis;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication(scanBasePackages = {"com.exam.cloud.analysis", "com.exam.cloud.common.web"})
@EnableDiscoveryClient
@EnableCaching
@MapperScan("com.exam.cloud.analysis.mapper")
public class AnalysisApplication {
    public static void main(String[] args) {
        SpringApplication.run(AnalysisApplication.class, args);
    }
}
