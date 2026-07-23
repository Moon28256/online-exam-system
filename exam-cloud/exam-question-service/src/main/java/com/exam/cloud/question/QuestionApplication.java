package com.exam.cloud.question;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication(scanBasePackages = {"com.exam.cloud.question", "com.exam.cloud.common.web"})
@EnableDiscoveryClient
@EnableCaching
@MapperScan("com.exam.cloud.question.mapper")
public class QuestionApplication {
    public static void main(String[] args) {
        SpringApplication.run(QuestionApplication.class, args);
    }
}
