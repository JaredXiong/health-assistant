package com.healthy.controller;

import com.healthy.dto.PrescriptionRecordDTO;
import com.healthy.result.PageResult;
import com.healthy.result.Result;
import com.healthy.service.PrescriptionService;
import com.healthy.vo.PrescriptionRecordVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/prescription")
@RequiredArgsConstructor
@Api(tags = "用药档案")
public class PrescriptionController {

    private final PrescriptionService prescriptionService;

    @GetMapping("/records")
    @ApiOperation("获取就诊记录列表")
    public Result<PageResult> getRecords(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        PageResult result = prescriptionService.getRecords(page, pageSize);
        return Result.success(result);
    }

    @GetMapping("/record/{id}")
    @ApiOperation("获取就诊记录详情")
    public Result<PrescriptionRecordVO> getRecordDetail(@PathVariable Long id) {
        PrescriptionRecordVO vo = prescriptionService.getRecordDetail(id);
        return Result.success(vo);
    }

    @PostMapping("/record")
    @ApiOperation("新增就诊记录")
    public Result<Long> addRecord(@RequestBody @Valid PrescriptionRecordDTO dto) {
        Long id = prescriptionService.addRecord(dto);
        return Result.success(id);
    }

    @PutMapping("/record/{id}")
    @ApiOperation("修改就诊记录")
    public Result<String> updateRecord(@PathVariable Long id, @RequestBody PrescriptionRecordDTO dto) {
        prescriptionService.updateRecord(id, dto);
        return Result.success("更新成功");
    }

    @DeleteMapping("/record/{id}")
    @ApiOperation("删除就诊记录")
    public Result<String> deleteRecord(@PathVariable Long id) {
        prescriptionService.deleteRecord(id);
        return Result.success("删除成功");
    }
}