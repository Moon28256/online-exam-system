package com.exam.cloud.paper.service;

import com.exam.cloud.common.dto.PaperCreateDTO;
import com.exam.cloud.common.entity.Paper;
import com.exam.cloud.common.entity.PaperQuestion;

import java.util.List;
import java.util.Map;

public interface PaperService {
    boolean create(PaperCreateDTO dto);
    boolean update(Long id, PaperCreateDTO dto);
    boolean delete(Long id);
    boolean publish(Long id);
    boolean unpublish(Long id);
    Paper getById(Long id);
    List<Paper> list(String status);
    List<Map<String, Object>> getQuestions(Long paperId);

    /** 内部：返回试卷关联的原始 PaperQuestion 列表（供 exam-service 组装考试） */
    List<PaperQuestion> getPaperQuestions(Long paperId);
}
