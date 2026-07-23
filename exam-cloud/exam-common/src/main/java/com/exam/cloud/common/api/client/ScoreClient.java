package com.exam.cloud.common.api.client;

import com.exam.cloud.common.api.Result;
import com.exam.cloud.common.api.dto.ScoreCreateDTO;
import com.exam.cloud.common.feign.FeignClientConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 成绩服务 Feign 客户端（供 exam-service 提交时写入成绩快照）。
 */
@FeignClient(name = "exam-score-service", path = "/score", configuration = FeignClientConfig.class)
public interface ScoreClient {

    @PostMapping("/internal")
    Result<Boolean> create(@RequestBody ScoreCreateDTO dto);
}
