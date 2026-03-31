package com.healthy.service.impl;

import com.baidu.aip.ocr.AipOcr;
import com.healthy.dto.OCRResultDTO;
import com.healthy.properties.OcrProperties;
import com.healthy.service.OcrService;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class OcrServiceImpl implements OcrService {

    private final AipOcr client;

    public OcrServiceImpl(OcrProperties ocrProperties) {
        client = new AipOcr(ocrProperties.getAppId(), ocrProperties.getApiKey(), ocrProperties.getSecretKey());
        client.setConnectionTimeoutInMillis(5000);
        client.setSocketTimeoutInMillis(10000);
    }

    @Override
    public OCRResultDTO recognizeMedicine(MultipartFile file) throws IOException {
        return recognizeMedicine(file.getBytes(), file.getOriginalFilename());
    }

    @Override
    public OCRResultDTO recognizeMedicine(byte[] imageBytes, String fileName) throws IOException {
        HashMap<String, String> options = new HashMap<>();
        options.put("language_type", "CHN_ENG");
        options.put("detect_direction", "true");
        options.put("probability", "false");

        JSONObject res = client.basicGeneral(imageBytes, options);
        log.info("OCR原始结果: {}", res.toString());

        if (res.has("error_code") && res.getInt("error_code") != 0) {
            String errorMsg = res.optString("error_msg", "未知错误");
            throw new RuntimeException("OCR识别失败，错误码：" + res.getInt("error_code") + "，错误信息：" + errorMsg);
        }

        return parseOcrResult(res);
    }

    private String extractFullText(JSONObject res) {
        JSONArray wordsResult = res.getJSONArray("words_result");
        StringBuilder fullText = new StringBuilder();
        for (int i = 0; i < wordsResult.length(); i++) {
            JSONObject item = wordsResult.getJSONObject(i);
            fullText.append(item.getString("words")).append("\n");
        }
        return fullText.toString();
    }

    private String cleanString(String str) {
        if (str == null) return null;
        return str.trim().replaceAll("\\s+", " ");
    }

    private OCRResultDTO parseOcrResult(JSONObject res) {
        OCRResultDTO dto = new OCRResultDTO();
        String text = extractFullText(res);
        dto.setRawText(text);

        dto.setMedicineName(cleanString(extractMedicineName(text)));
        dto.setSpecification(cleanString(extractSpecification(text)));
        dto.setManufacturer(cleanString(extractManufacturer(text)));
        dto.setApprovalNumber(cleanString(extractApprovalNumber(text)));
        dto.setUsageDosage(cleanString(extractUsageDosage(text)));
        dto.setIngredients(cleanString(extractIngredients(text)));
        dto.setIndications(cleanString(extractIndications(text)));
        dto.setAdverseReactions(cleanString(extractAdverseReactions(text)));
        dto.setPrecautions(cleanString(extractPrecautions(text)));
        dto.setContraindications(cleanString(extractContraindications(text)));

        dto.setOtherInfo(new ArrayList<>());
        return dto;
    }

    /**
     * 药品名称：优先匹配有实际内容的关键词（如通用名称），若都失败则启发式取行。
     */
    private String extractMedicineName(String text) {
        // 按优先级尝试各个关键词，取第一个有实际内容的
        String[] nameKeywords = {"通用名称", "商品名", "药品名称", "【药品名称】", "产品名称"};
        for (String kw : nameKeywords) {
            String name = extractByKeyword(text, kw);
            if (name != null && !name.trim().isEmpty()) {
                // 清洗：去除开头的冒号、空格等非中文字符
                name = name.replaceAll("^[:：\\s]+", "");
                if (!name.isEmpty()) {
                    return name;
                }
            }
        }

        // 启发式提取：遍历行，排除明显非药名的行
        String[] lines = text.split("\n");
        for (String line : lines) {
            String trimmed = line.trim();
            // 排除包含常见非药名关键词的行
            if (trimmed.contains("请仔细阅读") || trimmed.contains("成份") || trimmed.contains("功能主治")
                    || trimmed.contains("用法用量") || trimmed.contains("生产地址") || trimmed.contains("注册地址")
                    || trimmed.contains("【药品名称】") || trimmed.contains("汉语拼音") || trimmed.contains("性状")
                    || trimmed.contains("规格") || trimmed.contains("不良反应") || trimmed.contains("注意事项")
                    || trimmed.contains("禁忌")) {
                continue;
            }
            if (trimmed.length() >= 2 && trimmed.length() <= 30 && !trimmed.matches(".*\\d+.*")) {
                return trimmed;
            }
        }
        return null;
    }

    /**
     * 规格：关键词段落提取，并去除开头可能的括号
     */
    private String extractSpecification(String text) {
        String[] keywords = {"规格", "規格"};
        for (String kw : keywords) {
            String spec = extractParagraph(text, kw);
            if (spec != null && !spec.isEmpty()) {
                spec = spec.replaceAll("^[\\[\\]【】]*", "");
                return spec;
            }
        }
        return null;
    }

    /**
     * 用法用量：关键词段落提取，清洗开头和末尾噪声
     */
    private String extractUsageDosage(String text) {
        String[] keywords = {"用法用量", "用量", "用法", "服用方法"};
        String usage = tryExtractParagraph(text, keywords);
        if (usage == null) return null;

        usage = usage.replaceAll("^[\\[\\]【】()（）:：\\s]*", "");
        usage = usage.replaceAll("^(用法用量|用量|用法|服用方法)", "");
        usage = usage.replaceAll("^[】]+", "");
        usage = usage.replaceAll("[吃光]$", "");
        usage = usage.replaceAll("[A-Za-z]+$", "");
        return usage;
    }

    /**
     * 成份：关键词段落提取，清洗开头残留符号和末尾噪声
     */
    private String extractIngredients(String text) {
        String[] keywords = {"成分", "成份"};
        String ingredients = tryExtractParagraph(text, keywords);
        if (ingredients == null) return null;

        ingredients = ingredients.replaceAll("^成分|^成份", "");
        ingredients = ingredients.replaceAll("^[:：\\s]*", "");
        ingredients = ingredients.replaceAll("^[】]+", "");
        ingredients = ingredients.replaceAll("[、，,][A-Za-z]+$", "");
        ingredients = ingredients.replaceAll("[\\s习HhJj于引]+$", "");
        return ingredients;
    }

    private String extractIndications(String text) {
        String indications = extractParagraph(text, "功能主治");
        if (indications == null) return null;
        return indications;
    }

    private String extractAdverseReactions(String text) {
        String reactions = extractParagraph(text, "不良反应");
        if (reactions == null) return null;
        return reactions;
    }

    private String extractPrecautions(String text) {
        String precautions = extractParagraph(text, "注意事项");
        if (precautions == null) return null;
        return precautions;
    }

    private String extractContraindications(String text) {
        String contraindications = extractParagraph(text, "禁忌");
        if (contraindications == null) return null;
        return contraindications;
    }

    private String extractManufacturer(String text) {
        String manufacturer = extractByKeyword(text, "生产企业|生产厂家|制造商|厂家");
        if (manufacturer != null && !manufacturer.isEmpty()) {
            manufacturer = manufacturer.replaceAll("^[:：\\s]+", "");
            return manufacturer;
        }

        String addr = extractByKeyword(text, "生产地址|注册地址");
        if (addr != null && !addr.isEmpty()) {
            addr = addr.replaceAll("^[:：\\s]+", "");
            String[] parts = addr.split("[，,、\\s]");
            if (parts.length > 0) {
                return parts[0].trim();
            }
        }
        return null;
    }

    private String extractApprovalNumber(String text) {
        Pattern p = Pattern.compile("(国药准字[HZSB]\\d+|进口药品注册证号[\\w]+)");
        Matcher m = p.matcher(text);
        if (m.find()) {
            return m.group();
        }
        return null;
    }

    private String tryExtractParagraph(String text, String[] keywords) {
        for (String keyword : keywords) {
            String result = extractParagraph(text, keyword);
            if (result != null && !result.isEmpty()) {
                return result;
            }
        }
        return null;
    }

    /**
     * 提取关键词后的整段内容，允许关键词字符间有空白（包括换行），
     * 自动截断到下一个【或[之前，保证内容完整。
     */
    private String extractParagraph(String text, String keyword) {
        StringBuilder keywordPattern = new StringBuilder();
        for (char c : keyword.toCharArray()) {
            if (keywordPattern.length() > 0) {
                keywordPattern.append("\\s*");
            }
            keywordPattern.append(Pattern.quote(String.valueOf(c)));
        }
        String regex = "(?:【|\\[)?\\s*" + keywordPattern + "\\s*(?:】|\\])?\\s*([\\s\\S]*?)(?=(?:【|\\[)|$)";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(text);
        if (m.find()) {
            String content = m.group(1);
            return content == null ? null : content.trim();
        }
        return null;
    }

    private String extractByKeyword(String text, String keywordPattern) {
        String[] lines = text.split("\n");
        Pattern p = Pattern.compile(keywordPattern);
        for (String line : lines) {
            Matcher m = p.matcher(line);
            if (m.find()) {
                return line.replaceFirst(keywordPattern, "").trim();
            }
        }
        return null;
    }
}