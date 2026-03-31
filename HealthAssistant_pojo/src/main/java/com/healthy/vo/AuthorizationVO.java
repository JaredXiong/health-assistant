package com.healthy.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class AuthorizationVO {
    private Long authId;
    private Long grantorId;
    private String grantorName;
    private Long granteeId;
    private String granteeName;
    private List<String> dataTypes;
    private LocalDateTime expireTime;
}