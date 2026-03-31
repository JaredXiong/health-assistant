package com.healthy.mapper;

import com.healthy.entity.HealthData;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface HealthDataMapper {
    int insert(HealthData healthData);

    HealthData selectById(Long id);

    List<HealthData> selectByUserId(@Param("userId") Long userId,
                                    @Param("startTime") LocalDateTime startTime,
                                    @Param("endTime") LocalDateTime endTime);

    HealthData selectLatestByUserId(Long userId);

    // 分页查询
    List<HealthData> selectByUserIdWithPage(@Param("userId") Long userId,
                                            @Param("startTime") LocalDateTime startTime,
                                            @Param("endTime") LocalDateTime endTime,
                                            @Param("offset") int offset,
                                            @Param("limit") int limit,
                                            @Param("excludeId") Long excludeId);

    // 统计总数
    Long countByUserId(@Param("userId") Long userId,
                       @Param("startTime") LocalDateTime startTime,
                       @Param("endTime") LocalDateTime endTime,
                       @Param("excludeId") Long excludeId);

    List<HealthData> selectByUserIdWithRange(@Param("userId") Long userId,
                                             @Param("startTime") LocalDateTime startTime,
                                             @Param("endTime") LocalDateTime endTime);
}