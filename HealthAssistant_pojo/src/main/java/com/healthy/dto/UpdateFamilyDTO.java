package com.healthy.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class UpdateFamilyDTO {
    @NotBlank(message = "家庭名称不能为空")
    private String familyName;
}