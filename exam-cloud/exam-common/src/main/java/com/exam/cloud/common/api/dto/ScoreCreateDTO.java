package com.exam.cloud.common.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * exam-service 提交考试后调用 score-service 写入成绩快照的载荷。
 * examRecordId 用于幂等（重试防重）。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScoreCreateDTO {
    private Long userId;
    private Long paperId;
    private Integer score;
    private Long examRecordId;
    private LocalDateTime submitTime;
}
