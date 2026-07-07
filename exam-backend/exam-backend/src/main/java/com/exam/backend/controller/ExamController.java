package com.exam.backend.controller;

import com.exam.backend.common.Result;
import com.exam.backend.dto.ExamStartDTO;
import com.exam.backend.dto.SubmitAnswerDTO;
import com.exam.backend.service.ExamService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/exam")
public class ExamController {

    private final ExamService examService;

    public ExamController(ExamService examService) {
        this.examService = examService;
    }

    /** 开始考试 */
    @PostMapping("/start")
    public Result<Map<String, Object>> start(@RequestBody ExamStartDTO dto, HttpServletRequest request) {
        Long userId = getUserId(request);
        try {
            Map<String, Object> data = examService.startExam(userId, dto.getPaperId());
            return Result.success(data);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    /** 保存单题答案 */
    @PostMapping("/answer")
    public Result<String> submitAnswer(@RequestBody SubmitAnswerDTO dto, HttpServletRequest request) {
        Long userId = getUserId(request);
        try {
            examService.submitAnswer(userId, dto.getExamRecordId(), dto.getQuestionId(), dto.getUserAnswer());
            return Result.success("答案已保存");
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    /** 交卷 */
    @PostMapping("/submit/{examRecordId}")
    public Result<Map<String, Object>> submit(@PathVariable Long examRecordId, HttpServletRequest request) {
        Long userId = getUserId(request);
        try {
            Map<String, Object> result = examService.submitExam(userId, examRecordId);
            return Result.success(result);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    /** 考试详情（含批改结果） */
    @GetMapping("/record/{id}")
    public Result<Map<String, Object>> getDetail(@PathVariable Long id) {
        try {
            Map<String, Object> data = examService.getExamDetail(id);
            return Result.success(data);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    /** 我的考试记录 */
    @GetMapping("/my-list")
    public Result<List<Map<String, Object>>> myList(HttpServletRequest request) {
        Long userId = getUserId(request);
        List<Map<String, Object>> list = examService.getMyExamList(userId);
        return Result.success(list);
    }

    private Long getUserId(HttpServletRequest request) {
        Object uid = request.getAttribute("userId");
        if (uid == null) throw new RuntimeException("未登录");
        return Long.valueOf(uid.toString());
    }
}
