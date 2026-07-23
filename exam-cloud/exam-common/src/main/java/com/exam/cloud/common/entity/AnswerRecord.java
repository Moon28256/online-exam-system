package com.exam.cloud.common.entity;

import java.io.Serializable;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("answer_record")
public class AnswerRecord implements Serializable {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long examRecordId;
    private Long questionId;
    private String userAnswer;      // 学生答案，如 "A" / "A,C" / "Hello World"
    private Integer isCorrect;      // 0=错误 1=正确
    private Integer score;          // 该题得分
}
