package com.exam.backend.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface AnalysisMapper {

    /** 概览统计 — 各表数据量 */
    @Select("SELECT 'users' as name, COUNT(*) as value FROM user " +
            "UNION ALL SELECT 'questions', COUNT(*) FROM question " +
            "UNION ALL SELECT 'papers', COUNT(*) FROM paper " +
            "UNION ALL SELECT 'exams', COUNT(*) FROM exam_record " +
            "UNION ALL SELECT 'scores', COUNT(*) FROM score")
    List<Map<String, Object>> overview();

    /** 某试卷的成绩分布（按分数段） */
    @Select("""
        SELECT
            CASE
                WHEN score >= p.total_score * 0.9 THEN '优秀 (≥90%)'
                WHEN score >= p.total_score * 0.8 THEN '良好 (80-89%)'
                WHEN score >= p.total_score * 0.7 THEN '中等 (70-79%)'
                WHEN score >= p.total_score * 0.6 THEN '及格 (60-69%)'
                ELSE '不及格 (<60%)'
            END as range_name,
            COUNT(*) as count
        FROM score s
        JOIN paper p ON s.paper_id = p.id
        WHERE s.paper_id = #{paperId}
        GROUP BY range_name
        ORDER BY MIN(score)
    """)
    List<Map<String, Object>> scoreDistribution(@Param("paperId") Long paperId);

    /** 高频错题 Top10 */
    @Select("""
        SELECT q.content, q.type, q.category, q.difficulty,
               SUM(wq.wrong_count) as total_wrong,
               COUNT(DISTINCT wq.user_id) as student_count
        FROM wrong_question wq
        JOIN question q ON wq.question_id = q.id
        GROUP BY wq.question_id, q.content, q.type, q.category, q.difficulty
        ORDER BY total_wrong DESC
        LIMIT 10
    """)
    List<Map<String, Object>> mostMissedQuestions();

    /** 按类别统计正确率（基于答题记录） */
    @Select("""
        SELECT q.category,
               COUNT(*) as total_answers,
               SUM(ar.is_correct) as correct_count,
               ROUND(SUM(ar.is_correct) * 100.0 / COUNT(*), 1) as correct_rate
        FROM answer_record ar
        JOIN question q ON ar.question_id = q.id
        GROUP BY q.category
        ORDER BY correct_rate
    """)
    List<Map<String, Object>> categoryPerformance();

    /** 按题型统计正确率 */
    @Select("""
        SELECT q.type,
               COUNT(*) as total_answers,
               SUM(ar.is_correct) as correct_count,
               ROUND(SUM(ar.is_correct) * 100.0 / COUNT(*), 1) as correct_rate
        FROM answer_record ar
        JOIN question q ON ar.question_id = q.id
        GROUP BY q.type
        ORDER BY correct_rate
    """)
    List<Map<String, Object>> typePerformance();

    /** 最近考试活动（按日期统计） */
    @Select("""
        SELECT DATE(submit_time) as exam_date,
               COUNT(*) as exam_count,
               ROUND(AVG(total_score), 1) as avg_score
        FROM exam_record
        WHERE status = 'submitted'
        GROUP BY DATE(submit_time)
        ORDER BY exam_date DESC
        LIMIT 14
    """)
    List<Map<String, Object>> recentActivity();
}
