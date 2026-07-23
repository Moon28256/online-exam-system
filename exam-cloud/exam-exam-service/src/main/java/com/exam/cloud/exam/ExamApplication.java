package com.exam.cloud.exam;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {"com.exam.cloud.exam", "com.exam.cloud.common.web"})
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.exam.cloud.common.api.client")
@EnableScheduling
@MapperScan("com.exam.cloud.exam.mapper")
public class ExamApplication {
    public static void main(String[] args) {
        SpringApplication.run(ExamApplication.class, args);
    }
}
