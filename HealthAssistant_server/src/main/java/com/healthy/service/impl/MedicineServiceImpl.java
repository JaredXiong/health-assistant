package com.healthy.service.impl;

import com.alibaba.fastjson.JSON;
import com.healthy.dto.OCRResultDTO;
import com.healthy.entity.MedicineImageRecord;
import com.healthy.entity.MedicineInfo;
import com.healthy.mapper.MedicineImageRecordMapper;
import com.healthy.mapper.MedicineInfoMapper;
import com.healthy.service.MedicineService;
import com.healthy.service.OcrService;
import com.healthy.utils.AliOssUtil;
import com.healthy.utils.ImageCompressUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
public class MedicineServiceImpl implements MedicineService {

    private final OcrService ocrService;
    private final AliOssUtil aliOssUtil;
    private final MedicineInfoMapper medicineInfoMapper;
    //private final MedicationReminderMapper reminderMapper;
    private final MedicineImageRecordMapper imageRecordMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OCRResultDTO uploadAndRecognize(Long userId, MultipartFile file) throws IOException {
        byte[] originalBytes = file.getBytes();
        byte[] compressedBytes;
        try {
            compressedBytes = ImageCompressUtil.compressToMaxSize(originalBytes, 4 * 1024 * 1024);
            log.info("图片压缩完成，原始大小：{}，压缩后大小：{}", originalBytes.length, compressedBytes.length);
        } catch (IOException e) {
            log.error("图片压缩失败，将使用原始图片进行OCR", e);
            compressedBytes = originalBytes;
        }

        String fileName = "medicine/" + userId + "/" + System.currentTimeMillis() + "_" + file.getOriginalFilename();
        String imageUrl = aliOssUtil.upload(originalBytes, fileName);
        log.info("图片上传成功，URL：{}", imageUrl);

        MedicineImageRecord record = new MedicineImageRecord();
        record.setUserId(userId);
        record.setImageUrl(imageUrl);
        record.setStatus(0);
        imageRecordMapper.insert(record);
        log.info("图片记录已创建，ID：{}", record.getId());

        OCRResultDTO ocrResult;
        try {
            ocrResult = ocrService.recognizeMedicine(compressedBytes, file.getOriginalFilename());
        } catch (Exception e) {
            log.error("OCR识别失败", e);
            record.setStatus(2);
            record.setOcrRawResult(e.getMessage());
            imageRecordMapper.update(record);
            throw new RuntimeException("OCR识别失败", e);
        }

        // 设置图片URL到返回结果
        ocrResult.setImageUrl(imageUrl);

        record.setStatus(1);
        record.setOcrRawResult(JSON.toJSONString(ocrResult));
        record.setParsedInfo(JSON.toJSONString(ocrResult));
        record.setIngredients(ocrResult.getIngredients());
        record.setIndications(ocrResult.getIndications());
        record.setAdverseReactions(ocrResult.getAdverseReactions());
        record.setPrecautions(ocrResult.getPrecautions());
        record.setContraindications(ocrResult.getContraindications());
        imageRecordMapper.update(record);
        log.info("OCR识别成功，记录已更新");

//        // 药品信息去重入库
//        if (ocrResult.getMedicineName() != null && !ocrResult.getMedicineName().isEmpty()) {
//            MedicineInfo exist = medicineInfoMapper.selectByNameAndApproval(
//                    ocrResult.getMedicineName(), ocrResult.getApprovalNumber());
//            if (exist == null) {
//                MedicineInfo info = new MedicineInfo();
//                info.setName(ocrResult.getMedicineName());
//                info.setSpecification(ocrResult.getSpecification());
//                info.setManufacturer(ocrResult.getManufacturer());
//                info.setApprovalNumber(ocrResult.getApprovalNumber());
//                info.setUsageDosage(ocrResult.getUsageDosage());
//                info.setIngredients(ocrResult.getIngredients());
//                info.setIndications(ocrResult.getIndications());
//                info.setAdverseReactions(ocrResult.getAdverseReactions());
//                info.setPrecautions(ocrResult.getPrecautions());
//                info.setContraindications(ocrResult.getContraindications());
//                medicineInfoMapper.insert(info);
//                log.info("药品信息入库，ID：{}", info.getId());
//            } else {
//                log.info("药品已存在，ID：{}", exist.getId());
//            }
//        }

        // 药品信息去重入库
        if (ocrResult.getMedicineName() != null && !ocrResult.getMedicineName().isEmpty()) {
            MedicineInfo exist = medicineInfoMapper.selectByNameAndApproval(
                    ocrResult.getMedicineName(), ocrResult.getApprovalNumber());
            if (exist == null) {
                MedicineInfo info = new MedicineInfo();
                info.setName(ocrResult.getMedicineName());
                info.setSpecification(ocrResult.getSpecification());
                info.setManufacturer(ocrResult.getManufacturer());
                info.setApprovalNumber(ocrResult.getApprovalNumber());
                info.setUsageDosage(ocrResult.getUsageDosage());
                info.setIngredients(ocrResult.getIngredients());
                info.setIndications(ocrResult.getIndications());
                info.setAdverseReactions(ocrResult.getAdverseReactions());
                info.setPrecautions(ocrResult.getPrecautions());
                info.setContraindications(ocrResult.getContraindications());
                medicineInfoMapper.insert(info);
                ocrResult.setMedicineId(info.getId());   // 设置新生成的ID
                log.info("药品信息入库，ID：{}", info.getId());
            } else {
                ocrResult.setMedicineId(exist.getId());  // 设置已存在的ID
                log.info("药品已存在，ID：{}", exist.getId());
            }
        }

        return ocrResult;
    }

}