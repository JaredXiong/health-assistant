package com.healthy.service;

import com.healthy.dto.OCRResultDTO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface OcrService {
    /**
     * 识别图片中的药品信息（使用MultipartFile）
     */
    OCRResultDTO recognizeMedicine(MultipartFile file) throws IOException;

    /**
     * 识别图片中的药品信息（使用字节数组）
     */
    OCRResultDTO recognizeMedicine(byte[] imageBytes, String fileName) throws IOException;
}