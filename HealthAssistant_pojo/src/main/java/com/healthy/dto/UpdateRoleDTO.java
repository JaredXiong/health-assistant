package com.healthy.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class UpdateRoleDTO {
    @NotBlank(message = "角色不能为空")
    private String role; // PARENT 或 CHILD
}