package com.exam.cloud.exam.service;

import java.util.List;
import java.util.Map;

public interface ExamService {
    /** 开始考试，返回试卷信息和题目列表（不含答案） */
    Map<String, Object> startExam(Long userId, Long paperId);

    /** 保存单道题答案 */
    boolean submitAnswer(Long userId, Long examRecordId, Long questionId, String userAnswer);

    /** 交卷：自动批改，计算总分，跨服务最佳努力写入成绩/错题 */
    Map<String, Object> submitExam(Long userId, Long examRecordId);

    /** 考试详情（含批改结果、正确答案） */
    Map<String, Object> getExamDetail(Long examRecordId);

    /** 我的考试记录 */
    List<Map<String, Object>> getMyExamList(Long userId);
}
