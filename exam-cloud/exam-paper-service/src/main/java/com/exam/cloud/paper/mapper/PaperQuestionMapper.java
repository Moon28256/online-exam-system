package com.exam.cloud.paper.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.exam.cloud.common.entity.PaperQuestion;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface PaperQuestionMapper extends BaseMapper<PaperQuestion> {

    @Select("SELECT * FROM paper_question WHERE paper_id = #{paperId} ORDER BY sort_order")
    List<PaperQuestion> findByPaperId(@Param("paperId") Long paperId);

    @Delete("DELETE FROM paper_question WHERE paper_id = #{paperId}")
    int deleteByPaperId(@Param("paperId") Long paperId);

    @Insert("""
        INSERT INTO paper_question(paper_id, question_id, score, sort_order)
        VALUES(#{paperId}, #{questionId}, #{score}, #{sortOrder})
    """)
    int insert(PaperQuestion pq);
}
