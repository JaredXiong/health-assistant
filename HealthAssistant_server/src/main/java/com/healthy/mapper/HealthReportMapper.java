package com.healthy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.healthy.entity.HealthReport;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface HealthReportMapper extends BaseMapper<HealthReport> {
    int insert(HealthReport healthReport);

    HealthReport selectById(Long id);

    List<HealthReport> selectByUserId(@Param("userId") Long userId,
                                      @Param("startTime") LocalDateTime startTime,
                                      @Param("endTime") LocalDateTime endTime);

    HealthReport selectLatestByUserId(Long userId);
}