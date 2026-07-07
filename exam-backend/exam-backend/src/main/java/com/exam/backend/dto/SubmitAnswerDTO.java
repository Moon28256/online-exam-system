package com.exam.backend.dto;

import lombok.Data;

@Data
public class SubmitAnswerDTO {
    private Long examRecordId;
    private Long questionId;
    private String userAnswer;
}
