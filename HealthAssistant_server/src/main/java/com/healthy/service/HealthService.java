package com.healthy.service;

import com.healthy.dto.HealthDataDTO;
import com.healthy.dto.ManualSyncDTO;
import com.healthy.result.PageResult;  // 统一分页结果类
import com.healthy.vo.HealthReportVO;

import java.time.LocalDateTime;

public interface HealthService {
    /**
     * 手动同步健康数据并生成报告
     */
    HealthReportVO manualSyncHealthData(ManualSyncDTO dto);

    /**
     * 上传健康数据
     */
    Long uploadHealthData(HealthDataDTO dto);

    /**
     * 获取最新健康报告
     */
    HealthReportVO getLatestHealthReport();

    /**
     * 分析健康数据并生成报告
     */
    HealthReportVO analyzeAndGenerateReport(HealthDataDTO dto);

    PageResult queryHealthDataHistory(Integer page, Integer pageSize,
                                      LocalDateTime startTime, LocalDateTime endTime,
                                      Boolean excludeLatest); // 新增

    PageResult getHistoryReports(Long userId, Integer page, Integer pageSize);
}
