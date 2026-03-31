package com.healthy.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "ocr.baidu")
@Data
public class OcrProperties {
    private String appId;
    private String apiKey;
    private String secretKey;
}
