package com.exam.backend.service;

import com.exam.backend.entity.User;

public interface UserService {
    boolean register(User user);
    String login(String username, String password);  // 返回 token
    java.util.List<User> listAll();
    boolean deleteUser(Long id);

    // 个人中心
    User getProfile(Long userId);
    boolean updateProfile(Long userId, String realName, String email);
    boolean changePassword(Long userId, String oldPassword, String newPassword);
}
