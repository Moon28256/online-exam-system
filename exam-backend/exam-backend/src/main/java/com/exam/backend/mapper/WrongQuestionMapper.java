package com.exam.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.exam.backend.entity.WrongQuestion;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface WrongQuestionMapper extends BaseMapper<WrongQuestion> {

    @Select("""
        SELECT wq.*, q.type, q.content, q.answer, q.category, q.difficulty,
               q.option_a AS optionA, q.option_b AS optionB,
               q.option_c AS optionC, q.option_d AS optionD
        FROM wrong_question wq
        LEFT JOIN question q ON wq.question_id = q.id
        WHERE wq.user_id = #{userId}
        ORDER BY wq.last_wrong_time DESC
    """)
    List<Map<String, Object>> findMyWrongQuestions(@Param("userId") Long userId);

    @Select("SELECT * FROM wrong_question WHERE user_id = #{userId} AND question_id = #{questionId}")
    WrongQuestion findByUserAndQuestion(@Param("userId") Long userId, @Param("questionId") Long questionId);

    @Delete("DELETE FROM wrong_question WHERE user_id = #{userId} AND question_id = #{questionId}")
    int deleteByUserAndQuestion(@Param("userId") Long userId, @Param("questionId") Long questionId);

    /** 错题统计：按类别和题型汇总 */
    @Select("""
        SELECT q.category, q.type, COUNT(*) as count
        FROM wrong_question wq
        LEFT JOIN question q ON wq.question_id = q.id
        WHERE wq.user_id = #{userId}
        GROUP BY q.category, q.type
        ORDER BY COUNT(*) DESC
    """)
    List<Map<String, Object>> statisticsByUser(@Param("userId") Long userId);
}
