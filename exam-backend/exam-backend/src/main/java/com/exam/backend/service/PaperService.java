package com.exam.backend.service;

import com.exam.backend.dto.PaperCreateDTO;
import com.exam.backend.entity.Paper;

import java.util.List;
import java.util.Map;

public interface PaperService {
    boolean create(PaperCreateDTO dto);
    boolean update(Long id, PaperCreateDTO dto);
    boolean delete(Long id);
    boolean publish(Long id);
    boolean unpublish(Long id);   // 取消发布
    Paper getById(Long id);
    List<Paper> list(String status);
    List<Map<String, Object>> getQuestions(Long paperId);  // 获取试卷的题目列表
}
