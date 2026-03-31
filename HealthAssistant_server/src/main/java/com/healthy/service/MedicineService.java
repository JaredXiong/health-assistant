package com.healthy.service;

import com.healthy.dto.OCRResultDTO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface MedicineService {
    /**
     * 上传药盒图片并进行OCR识别
     */
    OCRResultDTO uploadAndRecognize(Long userId, MultipartFile file) throws IOException;

}