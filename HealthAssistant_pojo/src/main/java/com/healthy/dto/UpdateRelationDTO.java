package com.healthy.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class UpdateRelationDTO {
    @NotBlank(message = "关系称谓不能为空")
    private String relation;
}