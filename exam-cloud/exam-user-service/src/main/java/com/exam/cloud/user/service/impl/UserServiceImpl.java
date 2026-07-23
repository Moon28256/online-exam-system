package com.exam.cloud.user.service.impl;

import com.exam.cloud.common.auth.JwtUtil;
import com.exam.cloud.common.constant.RedisKey;
import com.exam.cloud.common.entity.User;
import com.exam.cloud.user.mapper.UserMapper;
import com.exam.cloud.user.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;
    private final StringRedisTemplate redis;
    private final ObjectMapper objectMapper;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Value("${exam.redis.token-ttl:86400}")
    private long tokenTtlSeconds;

    public UserServiceImpl(UserMapper userMapper, JwtUtil jwtUtil,
                           StringRedisTemplate redis, ObjectMapper objectMapper) {
        this.userMapper = userMapper;
        this.jwtUtil = jwtUtil;
        this.redis = redis;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean register(User user) {
        if (userMapper.findByUsername(user.getUsername()) != null) {
            return false;
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userMapper.insert(user) > 0;
    }

    @Override
    public String login(String username, String password) {
        User user = userMapper.findByUsername(username);
        if (user == null) return null;
        if (!passwordEncoder.matches(password, user.getPassword())) return null;

        // 签发 Token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());

        // 写入 Redis：login:token:<userId> -> token；login:user:<userId> -> 用户 JSON
        Duration ttl = Duration.ofSeconds(tokenTtlSeconds);
        redis.opsForValue().set(RedisKey.LOGIN_TOKEN + user.getId(), token, ttl);
        try {
            // 缓存不含密码的用户信息，供其他服务按 userId 取用户（如网关补充真实姓名）
            User safe = new User();
            safe.setId(user.getId());
            safe.setUsername(user.getUsername());
            safe.setRole(user.getRole());
            safe.setRealName(user.getRealName());
            safe.setEmail(user.getEmail());
            redis.opsForValue().set(RedisKey.LOGIN_USER + user.getId(),
                    objectMapper.writeValueAsString(safe), ttl);
        } catch (Exception ignored) {
        }
        return token;
    }

    @Override
    public List<User> listAll() {
        return userMapper.findAll();
    }

    @Override
    public boolean deleteUser(Long id) {
        boolean ok = userMapper.deleteUserById(id) > 0;
        if (ok) {
            redis.delete(RedisKey.LOGIN_TOKEN + id);
            redis.delete(RedisKey.LOGIN_USER + id);
        }
        return ok;
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
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) return false;
        return userMapper.updatePassword(userId, passwordEncoder.encode(newPassword)) > 0;
    }
}
