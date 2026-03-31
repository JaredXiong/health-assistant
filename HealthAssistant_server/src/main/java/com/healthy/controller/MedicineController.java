package com.healthy.controller;

import com.healthy.context.BaseContext;
import com.healthy.dto.OCRResultDTO;
import com.healthy.result.Result;
import com.healthy.service.MedicineService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/medicine")
@Slf4j
@RequiredArgsConstructor
@Api(tags = "药品识别与提醒")
public class MedicineController {

    private final MedicineService medicineService;

    @PostMapping("/upload")
    @ApiOperation("上传药盒图片并识别")
    public Result<OCRResultDTO> uploadImage(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("图片不能为空");
        }
        try {
            Long userId = BaseContext.getCurrentId(); // 从JWT中获取
            OCRResultDTO result = medicineService.uploadAndRecognize(userId, file);
            return Result.success(result);
        } catch (Exception e) {
            log.error("药品识别失败", e);
            return Result.error("识别失败：" + e.getMessage());
        }
    }
}
