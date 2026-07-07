package com.exam.backend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.exam.backend.mapper")
public class ExamBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExamBackendApplication.class, args);
    }

}
