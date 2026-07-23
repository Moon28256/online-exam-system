package com.exam.cloud.wrong.service;

import com.exam.cloud.common.api.dto.WrongBatchDTO;

import java.util.List;
import java.util.Map;

public interface WrongQuestionService {
    List<Map<String, Object>> getMyWrongQuestions(Long userId);
    List<Map<String, Object>> getStatistics(Long userId);
    boolean remove(Long userId, Long wrongQuestionId);
    boolean removeByQuestion(Long userId, Long questionId);

    /** 内部：考试提交后按批改结果维护错题本（按 examRecordId 幂等） */
    boolean submitBatch(WrongBatchDTO dto);
}
