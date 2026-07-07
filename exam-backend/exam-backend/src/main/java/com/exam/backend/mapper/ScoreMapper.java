package com.exam.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.exam.backend.entity.Score;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface ScoreMapper extends BaseMapper<Score> {

    @Select("SELECT * FROM score WHERE user_id = #{userId} ORDER BY submit_time DESC")
    List<Score> findByUserId(@Param("userId") Long userId);

    @Select("SELECT * FROM score WHERE paper_id = #{paperId} ORDER BY score DESC")
    List<Score> findByPaperId(@Param("paperId") Long paperId);

    @Select("""
        SELECT s.*, u.real_name, u.username
        FROM score s LEFT JOIN user u ON s.user_id = u.id
        WHERE s.paper_id = #{paperId}
        ORDER BY s.score DESC
    """)
    List<Map<String, Object>> findRankingByPaperId(@Param("paperId") Long paperId);

    @Select("""
        SELECT p.title as paper_title,
               COUNT(s.id) as student_count,
               AVG(s.score) as avg_score,
               MAX(s.score) as max_score,
               MIN(s.score) as min_score
        FROM score s LEFT JOIN paper p ON s.paper_id = p.id
        WHERE s.user_id = #{userId}
        GROUP BY s.paper_id, p.title
        ORDER BY AVG(s.score) DESC
    """)
    List<Map<String, Object>> statisticsByUser(@Param("userId") Long userId);

    @Select("""
        SELECT p.title as paper_title,
               COUNT(s.id) as student_count,
               AVG(s.score) as avg_score,
               MAX(s.score) as max_score,
               MIN(s.score) as min_score,
               p.total_score as full_score
        FROM score s LEFT JOIN paper p ON s.paper_id = p.id
        GROUP BY s.paper_id, p.title, p.total_score
        ORDER BY AVG(s.score) DESC
    """)
    List<Map<String, Object>> statisticsAll();
}
