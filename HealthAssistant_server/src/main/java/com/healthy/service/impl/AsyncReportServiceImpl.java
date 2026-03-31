package com.healthy.service.impl;

import com.healthy.dto.HealthDataDTO;
import com.healthy.entity.HealthReport;
import com.healthy.mapper.HealthReportMapper;
import com.healthy.service.AIGenerationService;
import com.healthy.service.AsyncReportService;
import com.healthy.service.HealthService;
import com.healthy.vo.HealthReportVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AsyncReportServiceImpl implements AsyncReportService {

    private final AIGenerationService aiGenerationService;
    private final HealthService healthService;          // 用于降级规则
    private final HealthReportMapper healthReportMapper;

    @Async("taskExecutor")  // 需要配置线程池
    @Override
    @Transactional
    public void generateReportAsync(Long userId, Long healthDataId, HealthDataDTO dto) {
        log.info("异步开始生成健康报告，用户ID：{}，健康数据ID：{}", userId, healthDataId);
        HealthReportVO reportVO;

        try {
            // 先尝试 AI 生成
            reportVO = aiGenerationService.generateReport(dto);
            if (reportVO == null) {
                // AI 失败，降级为规则生成
                reportVO = healthService.analyzeAndGenerateReport(dto);
            }
        } catch (Exception e) {
            log.error("异步生成报告异常，降级为规则生成", e);
            reportVO = healthService.analyzeAndGenerateReport(dto);
        }

        // 保存报告（复用原有逻辑，但需注意 healthDataId 关联）
        HealthReport healthReport = HealthReport.builder()
                .userId(userId)
                .healthDataId(healthDataId)
                .overallScore(reportVO.getOverallScore())
                .healthLevel(reportVO.getHealthLevel())
                .bloodPressureEval(reportVO.getBloodPressureEval())
                .bloodSugarEval(reportVO.getBloodSugarEval())
                .heartRateEval(reportVO.getHeartRateEval())
                .bloodOxygenEval(reportVO.getBloodOxygenEval())
                .bodyTemperatureEval(reportVO.getBodyTemperatureEval())
                .respiratoryRateEval(reportVO.getRespiratoryRateEval())
                .riskFactors(reportVO.getRiskFactors())
                .recommendations(reportVO.getRecommendations())
                .generateTime(LocalDateTime.now())
                .build();

        healthReportMapper.insert(healthReport);
        log.info("异步生成报告完成，报告ID：{}", healthReport.getId());
    }
}