package com.healthy.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class CreateFamilyDTO {
    @NotBlank(message = "家庭名称不能为空")
    private String familyName;
}