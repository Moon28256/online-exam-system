package com.exam.backend.controller;

import com.exam.backend.common.Result;
import com.exam.backend.dto.PaperCreateDTO;
import com.exam.backend.entity.Paper;
import com.exam.backend.service.PaperService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/paper")
public class PaperController {

    private final PaperService paperService;

    public PaperController(PaperService paperService) {
        this.paperService = paperService;
    }

    // 创建试卷（组卷）— 创建者从 token 中获取
    @PostMapping("/create")
    public Result<String> create(@RequestBody PaperCreateDTO dto,
                                  @RequestAttribute("userId") Long userId) {
        dto.setCreatorId(userId);
        boolean success = paperService.create(dto);
        return success ? Result.success("组卷成功") : Result.error("组卷失败");
    }

    // 更新试卷（仅创建者或管理员）
    @PutMapping("/{id}")
    public Result<String> update(@PathVariable Long id, @RequestBody PaperCreateDTO dto,
                                 @RequestAttribute("userId") Long userId,
                                 @RequestAttribute("role") String role) {
        Paper exist = paperService.getById(id);
        if (exist == null) return Result.error("试卷不存在");
        if (!"admin".equals(role) && !userId.equals(exist.getCreatorId())) {
            return Result.error("无权操作：只能修改自己创建的试卷");
        }
        boolean success = paperService.update(id, dto);
        return success ? Result.success("更新成功") : Result.error("更新失败");
    }

    // 删除试卷（仅创建者或管理员）
    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id,
                                 @RequestAttribute("userId") Long userId,
                                 @RequestAttribute("role") String role) {
        Paper exist = paperService.getById(id);
        if (exist == null) return Result.error("试卷不存在");
        if (!"admin".equals(role) && !userId.equals(exist.getCreatorId())) {
            return Result.error("无权操作：只能删除自己创建的试卷");
        }
        boolean success = paperService.delete(id);
        return success ? Result.success("删除成功") : Result.error("删除失败");
    }

    // 发布试卷（仅创建者或管理员）
    @PutMapping("/publish/{id}")
    public Result<String> publish(@PathVariable Long id,
                                  @RequestAttribute("userId") Long userId,
                                  @RequestAttribute("role") String role) {
        Paper exist = paperService.getById(id);
        if (exist == null) return Result.error("试卷不存在");
        if (!"admin".equals(role) && !userId.equals(exist.getCreatorId())) {
            return Result.error("无权操作：只能发布自己创建的试卷");
        }
        boolean success = paperService.publish(id);
        return success ? Result.success("发布成功") : Result.error("只能发布草稿状态的试卷");
    }

    // 取消发布（退回草稿）（仅创建者或管理员）
    @PutMapping("/unpublish/{id}")
    public Result<String> unpublish(@PathVariable Long id,
                                    @RequestAttribute("userId") Long userId,
                                    @RequestAttribute("role") String role) {
        Paper exist = paperService.getById(id);
        if (exist == null) return Result.error("试卷不存在");
        if (!"admin".equals(role) && !userId.equals(exist.getCreatorId())) {
            return Result.error("无权操作：只能取消发布自己创建的试卷");
        }
        boolean success = paperService.unpublish(id);
        return success ? Result.success("已退回草稿") : Result.error("只能取消已发布的试卷");
    }

    // 查询试卷详情
    @GetMapping("/{id}")
    public Result<Paper> getById(@PathVariable Long id) {
        Paper paper = paperService.getById(id);
        return paper != null ? Result.success(paper) : Result.error("试卷不存在");
    }

    // 获取试卷的题目列表
    @GetMapping("/{id}/questions")
    public Result<List<Map<String, Object>>> getQuestions(@PathVariable Long id) {
        List<Map<String, Object>> questions = paperService.getQuestions(id);
        return Result.success(questions);
    }

    // 试卷列表（按状态筛选）
    @GetMapping("/list")
    public Result<List<Paper>> list(@RequestParam(required = false) String status) {
        List<Paper> list = paperService.list(status);
        return Result.success(list);
    }
}
