package com.exam.backend.controller;

import com.exam.backend.common.Result;
import com.exam.backend.dto.LoginDTO;
import com.exam.backend.dto.RegisterDTO;
import com.exam.backend.dto.UpdateProfileDTO;
import com.exam.backend.dto.ChangePasswordDTO;
import com.exam.backend.entity.User;
import com.exam.backend.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // 注册
    @PostMapping("/register")
    public Result<String> register(@RequestBody RegisterDTO dto) {
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(dto.getPassword());
        user.setRole(dto.getRole());
        user.setRealName(dto.getRealName());
        user.setEmail(dto.getEmail());

        boolean success = userService.register(user);
        return success ? Result.success("注册成功") : Result.error("用户名已存在");
    }

    // 登录
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody LoginDTO dto) {
        String token = userService.login(dto.getUsername(), dto.getPassword());
        if (token != null) {
            Map<String, Object> data = new HashMap<>();
            data.put("token", token);
            return Result.success(data);
        }
        return Result.error("账号或密码错误");
    }

    // 获取当前用户信息（需要登录）
    @GetMapping("/info")
    public Result<Map<String, Object>> info(@RequestAttribute("userId") Long userId,
                                            @RequestAttribute("username") String username,
                                            @RequestAttribute("role") String role) {
        Map<String, Object> data = new HashMap<>();
        data.put("userId", userId);
        data.put("username", username);
        data.put("role", role);
        // 补充真实姓名，供前端显示
        User u = userService.getProfile(userId);
        if (u != null) data.put("realName", u.getRealName());
        return Result.success(data);
    }

    // 用户列表（管理员）
    @GetMapping("/list")
    public Result<java.util.List<Map<String, Object>>> list(@RequestAttribute("role") String role) {
        if (!"admin".equals(role)) return Result.error("无权访问");
        java.util.List<Map<String, Object>> list = new java.util.ArrayList<>();
        for (User u : userService.listAll()) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", u.getId());
            item.put("username", u.getUsername());
            item.put("role", u.getRole());
            item.put("realName", u.getRealName());
            item.put("email", u.getEmail());
            item.put("createdTime", u.getCreatedTime());
            list.add(item);
        }
        return Result.success(list);
    }

    // 删除用户（管理员）
    @DeleteMapping("/{id}")
    public Result<String> deleteUser(@PathVariable Long id, @RequestAttribute("role") String role) {
        if (!"admin".equals(role)) return Result.error("无权访问");
        boolean ok = userService.deleteUser(id);
        return ok ? Result.success("删除成功") : Result.error("删除失败");
    }

    // ========== 个人中心 ==========

    // 获取个人完整资料
    @GetMapping("/profile")
    public Result<Map<String, Object>> profile(@RequestAttribute("userId") Long userId) {
        User u = userService.getProfile(userId);
        if (u == null) return Result.error("用户不存在");
        Map<String, Object> data = new HashMap<>();
        data.put("id", u.getId());
        data.put("username", u.getUsername());
        data.put("role", u.getRole());
        data.put("realName", u.getRealName());
        data.put("email", u.getEmail());
        data.put("createdTime", u.getCreatedTime());
        return Result.success(data);
    }

    // 更新个人资料（真实姓名、邮箱）
    @PostMapping("/update-profile")
    public Result<String> updateProfile(@RequestBody UpdateProfileDTO dto,
                                        @RequestAttribute("userId") Long userId) {
        boolean ok = userService.updateProfile(userId, dto.getRealName(), dto.getEmail());
        return ok ? Result.success("资料更新成功") : Result.error("更新失败");
    }

    // 修改密码
    @PostMapping("/change-password")
    public Result<String> changePassword(@RequestBody ChangePasswordDTO dto,
                                         @RequestAttribute("userId") Long userId) {
        if (dto.getNewPassword() == null || dto.getNewPassword().length() < 6) {
            return Result.error("新密码长度不能少于6位");
        }
        boolean ok = userService.changePassword(userId, dto.getOldPassword(), dto.getNewPassword());
        return ok ? Result.success("密码修改成功") : Result.error("原密码错误");
    }
}
