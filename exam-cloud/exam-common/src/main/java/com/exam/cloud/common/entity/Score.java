package com.exam.cloud.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("score")
public class Score {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long userId;
    private Long paperId;
    private Integer score;
    private LocalDateTime submitTime;
}
