package com.exam.backend.controller;

import com.exam.backend.common.Result;
import com.exam.backend.service.WrongQuestionService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/wrong")
public class WrongQuestionController {

    private final WrongQuestionService wrongQuestionService;

    public WrongQuestionController(WrongQuestionService wrongQuestionService) {
        this.wrongQuestionService = wrongQuestionService;
    }

    /** 我的错题本 */
    @GetMapping("/my")
    public Result<List<Map<String, Object>>> myWrongQuestions(HttpServletRequest request) {
        Long userId = getUserId(request);
        List<Map<String, Object>> list = wrongQuestionService.getMyWrongQuestions(userId);
        return Result.success(list);
    }

    /** 错题统计（按类别/题型分布） */
    @GetMapping("/statistics")
    public Result<List<Map<String, Object>>> statistics(HttpServletRequest request) {
        Long userId = getUserId(request);
        List<Map<String, Object>> list = wrongQuestionService.getStatistics(userId);
        return Result.success(list);
    }

    /** 移除某道错题（按错题记录ID） */
    @DeleteMapping("/{id}")
    public Result<String> remove(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getUserId(request);
        boolean ok = wrongQuestionService.remove(userId, id);
        return ok ? Result.success("已移除") : Result.error("移除失败");
    }

    private Long getUserId(HttpServletRequest request) {
        Object uid = request.getAttribute("userId");
        if (uid == null) throw new RuntimeException("未登录");
        return Long.valueOf(uid.toString());
    }
}
