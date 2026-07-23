package com.exam.cloud.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.exam.cloud.common.entity.User;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    @Select("SELECT * FROM user WHERE username = #{username}")
    User findByUsername(String username);

    @Insert("""
        INSERT INTO user(username, password, role, real_name, email)
        VALUES(#{username}, #{password}, #{role}, #{realName}, #{email})
    """)
    int insert(User user);

    @Select("SELECT * FROM user ORDER BY id")
    List<User> findAll();

    @Delete("DELETE FROM user WHERE id = #{id}")
    int deleteUserById(@Param("id") Long id);

    @Update("UPDATE user SET real_name = #{realName}, email = #{email} WHERE id = #{id}")
    int updateProfile(@Param("id") Long id, @Param("realName") String realName, @Param("email") String email);

    @Update("UPDATE user SET password = #{password} WHERE id = #{id}")
    int updatePassword(@Param("id") Long id, @Param("password") String password);
}
