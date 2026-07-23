package com.exam.cloud.exam.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.exam.cloud.common.entity.ExamRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ExamRecordMapper extends BaseMapper<ExamRecord> {

    @Select("SELECT * FROM exam_record WHERE user_id = #{userId} ORDER BY start_time DESC")
    List<ExamRecord> findByUserId(@Param("userId") Long userId);

    @Select("SELECT * FROM exam_record WHERE user_id = #{userId} AND paper_id = #{paperId} AND status = 'in_progress'")
    ExamRecord findInProgress(@Param("userId") Long userId, @Param("paperId") Long paperId);
}
