package com.exam.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("paper_question")
public class PaperQuestion {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long paperId;
    private Long questionId;
    private Integer score;      // 该题在这张试卷中的分值
    private Integer sortOrder;  // 排序
}
