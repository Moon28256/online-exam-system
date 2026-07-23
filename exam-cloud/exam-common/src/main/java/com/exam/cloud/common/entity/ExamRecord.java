package com.exam.cloud.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("exam_record")
public class ExamRecord {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long userId;
    private Long paperId;
    private String status;          // in_progress | submitted
    private LocalDateTime startTime;
    private LocalDateTime submitTime;
    private Integer totalScore;     // 最终得分
}
