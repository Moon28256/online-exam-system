package com.exam.cloud.wrong;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication(scanBasePackages = {"com.exam.cloud.wrong", "com.exam.cloud.common.web"})
@EnableDiscoveryClient
@MapperScan("com.exam.cloud.wrong.mapper")
public class WrongApplication {
    public static void main(String[] args) {
        SpringApplication.run(WrongApplication.class, args);
    }
}
