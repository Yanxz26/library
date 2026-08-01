package com.library.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 登录请求DTO
 *
 * @author Library Team
 */
@Data
@Schema(description = "登录请求参数")
public class LoginDTO {

    @NotBlank(message = "账号不能为空")
    @Schema(description = "登录账号（学号/工号）", example = "2024001")
    private String userAccount;

    @NotBlank(message = "密码不能为空")
    @Schema(description = "登录密码", example = "123456")
    private String password;
}
