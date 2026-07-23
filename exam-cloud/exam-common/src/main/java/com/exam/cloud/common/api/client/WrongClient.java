package com.exam.cloud.common.api.client;

import com.exam.cloud.common.api.Result;
import com.exam.cloud.common.api.dto.WrongBatchDTO;
import com.exam.cloud.common.feign.FeignClientConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 错题服务 Feign 客户端（供 exam-service 提交时维护错题本）。
 */
@FeignClient(name = "exam-wrong-service", path = "/wrong", configuration = FeignClientConfig.class)
public interface WrongClient {

    @PostMapping("/internal/batch")
    Result<Boolean> submitBatch(@RequestBody WrongBatchDTO dto);
}
