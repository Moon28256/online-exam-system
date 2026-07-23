package com.exam.cloud.question.controller;

import com.exam.cloud.common.api.Result;
import com.exam.cloud.common.entity.Question;
import com.exam.cloud.question.service.QuestionService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/question")
public class QuestionController {

    private final QuestionService questionService;

    public QuestionController(QuestionService questionService) {
        this.questionService = questionService;
    }

    // 添加题目（创建者从 token 获取）
    @PostMapping("/add")
    public Result<String> add(@RequestBody Question question,
                              @RequestAttribute("userId") Long userId) {
        question.setCreatorId(userId);
        boolean success = questionService.add(question);
        return success ? Result.success("添加成功") : Result.error("添加失败");
    }

    // 删除题目（仅创建者或管理员）
    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id,
                                 @RequestAttribute("userId") Long userId,
                                 @RequestAttribute("role") String role) {
        Question q = questionService.getById(id);
        if (q == null) return Result.error("题目不存在");
        if (!"admin".equals(role) && !userId.equals(q.getCreatorId())) {
            return Result.error("无权操作：只能删除自己创建的题目");
        }
        boolean success = questionService.delete(id);
        return success ? Result.success("删除成功") : Result.error("删除失败");
    }

    // 更新题目（仅创建者或管理员）
    @PutMapping("/{id}")
    public Result<String> update(@PathVariable Long id, @RequestBody Question question,
                                 @RequestAttribute("userId") Long userId,
                                 @RequestAttribute("role") String role) {
        Question exist = questionService.getById(id);
        if (exist == null) return Result.error("题目不存在");
        if (!"admin".equals(role) && !userId.equals(exist.getCreatorId())) {
            return Result.error("无权操作：只能修改自己创建的题目");
        }
        question.setId(id);
        // 保留原创建者，防止篡改
        question.setCreatorId(exist.getCreatorId());
        boolean success = questionService.update(question);
        return success ? Result.success("更新成功") : Result.error("更新失败");
    }

    // 查询单个题目
    @GetMapping("/{id}")
    public Result<Question> getById(@PathVariable Long id) {
        Question question = questionService.getById(id);
        return question != null ? Result.success(question) : Result.error("题目不存在");
    }

    // 分页 + 筛选查询（所有教师共享查看）
    @GetMapping("/list")
    public Result<List<Question>> list(@RequestParam(required = false) String type,
                                        @RequestParam(required = false) String category,
                                        @RequestParam(required = false) String difficulty,
                                        @RequestParam(required = false) String keyword) {
        List<Question> list = questionService.list(type, category, difficulty, keyword);
        return Result.success(list);
    }

    // ==================== 服务间内部端点（不经网关） ====================

    @GetMapping("/internal/{id}")
    public Result<Question> internalGet(@PathVariable Long id) {
        return Result.success(questionService.getById(id));
    }

    @GetMapping("/internal/batch")
    public Result<List<Question>> internalBatch(@RequestParam("ids") List<Long> ids) {
        return Result.success(questionService.listByIds(ids));
    }
}
