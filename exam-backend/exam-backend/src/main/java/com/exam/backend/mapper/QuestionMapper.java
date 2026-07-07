package com.exam.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.exam.backend.entity.Question;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface QuestionMapper extends BaseMapper<Question> {

    @Select("""
        <script>
            SELECT * FROM question
            <where>
                <if test='type != null and type != ""'> AND type = #{type} </if>
                <if test='category != null and category != ""'> AND category = #{category} </if>
                <if test='difficulty != null and difficulty != ""'> AND difficulty = #{difficulty} </if>
                <if test='keyword != null and keyword != ""'>
                    AND content LIKE CONCAT('%', #{keyword}, '%')
                </if>
            </where>
            ORDER BY created_time DESC
        </script>
    """)
    List<Question> findByCondition(@Param("type") String type,
                                   @Param("category") String category,
                                   @Param("difficulty") String difficulty,
                                   @Param("keyword") String keyword);
}
