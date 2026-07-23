package com.exam.cloud.common.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * exam-service 提交考试后调用 wrong-service 维护错题本的批量载荷。
 * items 为本次考试每道题的批改结果；examRecordId 用于幂等。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WrongBatchDTO {
    private Long userId;
    private Long examRecordId;
    private List<Item> items;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Item {
        private Long questionId;
        private Boolean isCorrect;
    }
}
