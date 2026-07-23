package com.exam.cloud.user.service;

import com.exam.cloud.common.entity.User;

import java.util.List;

public interface UserService {
    boolean register(User user);
    /** 登录，校验通过则签发 Token 并写入 Redis，返回 token */
    String login(String username, String password);
    List<User> listAll();
    boolean deleteUser(Long id);

    // 个人中心
    User getProfile(Long userId);
    boolean updateProfile(Long userId, String realName, String email);
    boolean changePassword(Long userId, String oldPassword, String newPassword);
}
