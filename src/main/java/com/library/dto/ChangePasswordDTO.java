package com.library.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 修改密码DTO
 *
 * @author Library Team
 */
@Data
@Schema(description = "修改密码参数")
public class ChangePasswordDTO {

    @NotBlank(message = "原密码不能为空")
    @Schema(description = "原密码", required = true)
    private String oldPassword;

    @NotBlank(message = "新密码不能为空")
    @Schema(description = "新密码", required = true)
    private String newPassword;
}
