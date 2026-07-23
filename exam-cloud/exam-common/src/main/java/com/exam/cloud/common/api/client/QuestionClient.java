package com.exam.cloud.common.api.client;

import com.exam.cloud.common.api.Result;
import com.exam.cloud.common.entity.Question;
import com.exam.cloud.common.feign.FeignClientConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * 题目服务 Feign 客户端（供 paper-service / exam-service 调用）。
 * 路径 /question/internal/** 为服务间内部端点，不经网关。
 */
@FeignClient(name = "exam-question-service", path = "/question", configuration = FeignClientConfig.class)
public interface QuestionClient {

    @GetMapping("/internal/{id}")
    Result<Question> getById(@PathVariable("id") Long id);

    @GetMapping("/internal/batch")
    Result<List<Question>> batch(@org.springframework.web.bind.annotation.RequestParam("ids") List<Long> ids);
}
