package com.exam.cloud.common.api.client;

import com.exam.cloud.common.api.Result;
import com.exam.cloud.common.entity.Paper;
import com.exam.cloud.common.entity.PaperQuestion;
import com.exam.cloud.common.feign.FeignClientConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * 试卷服务 Feign 客户端（供 exam-service 调用）。
 */
@FeignClient(name = "exam-paper-service", path = "/paper", configuration = FeignClientConfig.class)
public interface PaperClient {

    @GetMapping("/internal/{id}")
    Result<Paper> getById(@PathVariable("id") Long id);

    /** 试卷关联的题目（含分值与排序，不含答案） */
    @GetMapping("/internal/{id}/paper-questions")
    Result<List<PaperQuestion>> getPaperQuestions(@PathVariable("id") Long id);
}
