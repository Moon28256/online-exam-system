package com.exam.cloud.common.dto;

import lombok.Data;
import java.util.List;

@Data
public class PaperCreateDTO {
    private String title;
    private String description;
    private Integer duration;
    private String startTime;
    private String endTime;
    private Integer totalScore;
    private Long creatorId;
    private List<QuestionItem> questions;  // 选题列表

    @Data
    public static class QuestionItem {
        private Long questionId;
        private Integer score;
    }
}
