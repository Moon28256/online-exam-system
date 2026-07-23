package com.exam.cloud.common.entity;

import java.io.Serializable;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

@Data
@TableName("paper")
public class Paper implements Serializable {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private String title;
    private String description;
    private Integer duration;      // 考试时长（分钟）
    private Date startTime;        // 考试开始时间
    private Date endTime;          // 考试结束时间
    private Integer totalScore;
    private String status;         // draft / published / ended
    private Long creatorId;
    private Date createdTime;
}
