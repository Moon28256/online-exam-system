package com.exam.backend.controller;

import com.exam.backend.common.Result;
import com.exam.backend.service.ScoreService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/score")
public class ScoreController {

    private final ScoreService scoreService;

    public ScoreController(ScoreService scoreService) {
        this.scoreService = scoreService;
    }

    /** 我的成绩汇总 */
    @GetMapping("/my")
    public Result<Map<String, Object>> myScores(HttpServletRequest request) {
        Long userId = getUserId(request);
        Map<String, Object> data = scoreService.getMyStatistics(userId);
        return Result.success(data);
    }

    /** 某试卷的成绩排名（教师查看） */
    @GetMapping("/ranking/{paperId}")
    public Result<List<Map<String, Object>>> ranking(@PathVariable Long paperId) {
        List<Map<String, Object>> list = scoreService.getPaperRanking(paperId);
        return Result.success(list);
    }

    /** 全部试卷统计（教师查看） */
    @GetMapping("/statistics")
    public Result<List<Map<String, Object>>> statistics() {
        List<Map<String, Object>> list = scoreService.getAllStatistics();
        return Result.success(list);
    }

    private Long getUserId(HttpServletRequest request) {
        Object uid = request.getAttribute("userId");
        if (uid == null) throw new RuntimeException("未登录");
        return Long.valueOf(uid.toString());
    }
}
