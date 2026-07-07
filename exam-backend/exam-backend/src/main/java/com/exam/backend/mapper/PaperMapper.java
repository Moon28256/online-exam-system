package com.exam.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.exam.backend.entity.Paper;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface PaperMapper extends BaseMapper<Paper> {

    @Select("SELECT * FROM paper ORDER BY created_time DESC")
    List<Paper> findAll();

    @Select("SELECT * FROM paper WHERE status = #{status} ORDER BY created_time DESC")
    List<Paper> findByStatus(@Param("status") String status);
}
