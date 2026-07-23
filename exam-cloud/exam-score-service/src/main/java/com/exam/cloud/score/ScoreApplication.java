package com.exam.cloud.score;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication(scanBasePackages = {"com.exam.cloud.score", "com.exam.cloud.common.web"})
@EnableDiscoveryClient
@EnableCaching
@MapperScan("com.exam.cloud.score.mapper")
public class ScoreApplication {
    public static void main(String[] args) {
        SpringApplication.run(ScoreApplication.class, args);
    }
}
