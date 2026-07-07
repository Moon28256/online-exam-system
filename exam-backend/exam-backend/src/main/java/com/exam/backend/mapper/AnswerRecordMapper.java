package com.exam.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.exam.backend.entity.AnswerRecord;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface AnswerRecordMapper extends BaseMapper<AnswerRecord> {

    @Select("SELECT * FROM answer_record WHERE exam_record_id = #{examRecordId}")
    List<AnswerRecord> findByExamRecordId(@Param("examRecordId") Long examRecordId);

    @Select("SELECT * FROM answer_record WHERE exam_record_id = #{examRecordId} AND question_id = #{questionId}")
    AnswerRecord findOne(@Param("examRecordId") Long examRecordId, @Param("questionId") Long questionId);

    @Delete("DELETE FROM answer_record WHERE exam_record_id = #{examRecordId}")
    int deleteByExamRecordId(@Param("examRecordId") Long examRecordId);
}
