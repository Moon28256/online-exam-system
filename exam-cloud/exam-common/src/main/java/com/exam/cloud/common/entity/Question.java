package com.exam.cloud.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

@Data
@TableName("question")
public class Question {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private String type;        // single_choice / multi_choice / true_false / fill_blank / essay
    private String category;    // 分类（如：Java基础、数据库）
    private String content;     // 题干
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
    private String answer;      // 答案
    private Integer score;      // 分值，默认5
    private String difficulty;  // easy / medium / hard
    private Long creatorId;     // 创建者ID
    private Date createdTime;
}
