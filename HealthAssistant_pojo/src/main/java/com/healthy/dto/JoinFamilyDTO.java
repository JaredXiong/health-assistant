package com.healthy.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class JoinFamilyDTO {
    @NotBlank(message = "邀请码不能为空")
    private String inviteCode;
}
