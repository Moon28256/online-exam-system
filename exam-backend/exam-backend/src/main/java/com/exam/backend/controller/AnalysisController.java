package com.exam.backend.controller;

import com.exam.backend.common.Result;
import com.exam.backend.service.AnalysisService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/analysis")
public class AnalysisController {

    private final AnalysisService analysisService;

    public AnalysisController(AnalysisService analysisService) {
        this.analysisService = analysisService;
    }

    /** 系统概览（教师/管理员） */
    @GetMapping("/overview")
    public Result<Map<String, Object>> overview() {
        return Result.success(analysisService.getOverview());
    }

    /** 某试卷成绩分布 */
    @GetMapping("/score-distribution/{paperId}")
    public Result<List<Map<String, Object>>> scoreDistribution(@PathVariable Long paperId) {
        return Result.success(analysisService.getScoreDistribution(paperId));
    }

    /** 高频错题 Top10 */
    @GetMapping("/most-missed")
    public Result<List<Map<String, Object>>> mostMissed() {
        return Result.success(analysisService.getMostMissedQuestions());
    }

    /** 各类别+题型正确率 */
    @GetMapping("/performance")
    public Result<Map<String, Object>> performance() {
        return Result.success(analysisService.getPerformance());
    }

    /** 我的成绩趋势 */
    @GetMapping("/my-trend")
    public Result<List<Map<String, Object>>> myTrend(HttpServletRequest request) {
        Long userId = getUserId(request);
        return Result.success(analysisService.getStudentTrend(userId));
    }

    /** 最近考试活动 */
    @GetMapping("/recent-activity")
    public Result<List<Map<String, Object>>> recentActivity() {
        return Result.success(analysisService.getRecentActivity());
    }

    private Long getUserId(HttpServletRequest request) {
        Object uid = request.getAttribute("userId");
        if (uid == null) throw new RuntimeException("未登录");
        return Long.valueOf(uid.toString());
    }
}
