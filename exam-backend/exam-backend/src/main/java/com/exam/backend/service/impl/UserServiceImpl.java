package com.exam.backend.service.impl;

import com.exam.backend.entity.User;
import com.exam.backend.mapper.UserMapper;
import com.exam.backend.service.UserService;
import com.exam.backend.util.JwtUtil;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserServiceImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public boolean register(User user) {
        // 判断用户名是否存在
        User dbUser = userMapper.findByUsername(user.getUsername());
        if (dbUser != null) {
            return false;
        }
        // 密码加密
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userMapper.insert(user) > 0;
    }

    @Override
    public String login(String username, String password) {
        User user = userMapper.findByUsername(username);
        if (user == null) {
            return null;
        }
        // BCrypt 密码验证
        if (passwordEncoder.matches(password, user.getPassword())) {
            return JwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        }
        return null;
    }

    @Override
    public java.util.List<User> listAll() {
        return userMapper.findAll();
    }

    @Override
    public boolean deleteUser(Long id) {
        return userMapper.deleteUserById(id) > 0;
    }

    @Override
    public User getProfile(Long userId) {
        return userMapper.selectById(userId);
    }

    @Override
    public boolean updateProfile(Long userId, String realName, String email) {
        return userMapper.updateProfile(userId, realName, email) > 0;
    }

    @Override
    public boolean changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userMapper.selectById(userId);
        if (user == null) return false;
        // 校验旧密码
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            return false;
        }
        return userMapper.updatePassword(userId, passwordEncoder.encode(newPassword)) > 0;
    }
}
