package com.healthy.controller;

import com.healthy.context.BaseContext;
import com.healthy.dto.HealthDataDTO;
import com.healthy.dto.ManualSyncDTO;
import com.healthy.result.PageResult;
import com.healthy.result.Result;
import com.healthy.service.AsyncReportService;
import com.healthy.service.HealthService;
import com.healthy.vo.HealthReportVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/health")
@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
@Api(tags = "健康数据管理")
public class HealthController {

    private final HealthService healthService;
    private final AsyncReportService asyncReportService;

    @PostMapping("/manual-sync")
    @ApiOperation("手动同步健康数据并生成报告")
    public Result<HealthReportVO> manualSyncHealthData(@RequestBody ManualSyncDTO dto) {
        try {
            log.info("收到手动同步请求: {}", dto);
            HealthReportVO report = healthService.manualSyncHealthData(dto);
            return Result.success(report);
        } catch (Exception e) {
            log.error("手动同步失败", e);
            return Result.error("同步失败: " + e.getMessage());
        }
    }

    @PostMapping("/upload")
    @ApiOperation("上传健康数据")
    public Result<Long> uploadHealthData(@RequestBody HealthDataDTO dto) {
        try {
            Long healthDataId = healthService.uploadHealthData(dto);
            return Result.success(healthDataId);
        } catch (Exception e) {
            log.error("健康数据上传失败", e);
            return Result.error("上传失败: " + e.getMessage());
        }
    }

    @GetMapping("/report/latest")
    @ApiOperation("获取最新健康报告")
    public Result<HealthReportVO> getLatestHealthReport() {
        try {
            HealthReportVO report = healthService.getLatestHealthReport();
            if (report == null) {
                return Result.error("暂无健康报告");
            }
            return Result.success(report);
        } catch (Exception e) {
            log.error("获取健康报告失败", e);
            return Result.error("获取失败: " + e.getMessage());
        }
    }

    @PostMapping("/analyze")
    @ApiOperation("分析健康数据（不保存）")
    public Result<HealthReportVO> analyzeHealthData(@RequestBody HealthDataDTO dto) {
        try {
            HealthReportVO report = healthService.analyzeAndGenerateReport(dto);
            return Result.success(report);
        } catch (Exception e) {
            log.error("健康数据分析失败", e);
            return Result.error("分析失败: " + e.getMessage());
        }
    }

    @GetMapping("/history")
    @ApiOperation("分页查询历史健康数据")
    public Result<PageResult> queryHistory(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam(required = false) Boolean excludeLatest) { // 新增参数
        try {
            PageResult pageResult = healthService.queryHealthDataHistory(page, pageSize, startTime, endTime, excludeLatest);
            return Result.success(pageResult);
        } catch (Exception e) {
            log.error("查询健康数据历史失败", e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 手动同步健康数据（异步生成报告）
     */
    @PostMapping("/manual-sync-async")
    @ApiOperation("手动同步健康数据（异步生成报告）")
    public Result<Long> manualSyncHealthDataAsync(@RequestBody ManualSyncDTO dto) {
        Long userId = BaseContext.getCurrentId();
        if (userId == null) {
            return Result.error("用户未登录");
        }
        // 转换为 HealthDataDTO
        HealthDataDTO healthDataDTO = convertToHealthDataDTO(dto);
        // 上传健康数据（同步保存）
        Long healthDataId = healthService.uploadHealthData(healthDataDTO);
        if (healthDataId == null) {
            return Result.error("保存健康数据失败");
        }

        // 触发异步生成报告
        asyncReportService.generateReportAsync(userId, healthDataId, healthDataDTO);

        return Result.success(healthDataId);
    }

    /**
     * 将 ManualSyncDTO 转换为 HealthDataDTO
     */
    private HealthDataDTO convertToHealthDataDTO(ManualSyncDTO dto) {
        HealthDataDTO healthDataDTO = new HealthDataDTO();
        healthDataDTO.setHeartRate(dto.getHeartRate());
        healthDataDTO.setSystolicBp(dto.getSystolicBp());
        healthDataDTO.setDiastolicBp(dto.getDiastolicBp());
        healthDataDTO.setBloodOxygen(dto.getBloodOxygen());
        healthDataDTO.setBloodSugar(dto.getBloodSugar());
        healthDataDTO.setBodyTemperature(dto.getBodyTemperature());
        healthDataDTO.setRespiratoryRate(dto.getRespiratoryRate());
        healthDataDTO.setMeasurementTime(LocalDateTime.now());
        return healthDataDTO;
    }

    /**
     * 获取历史健康报告列表
     */
    @GetMapping("/report/history")
    @ApiOperation("获取历史健康报告列表")
    public Result<PageResult> getHistoryReports(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Long userId = BaseContext.getCurrentId();
        if (userId == null) {
            return Result.error("用户未登录");
        }
        PageResult pageResult = healthService.getHistoryReports(userId, page, pageSize);
        return Result.success(pageResult);
    }
}