package com.library.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * 登录返回DTO
 *
 * @author Library Team
 */
@Data
@Builder
@Schema(description = "登录返回结果")
public class LoginResultDTO {

    @Schema(description = "JWT Token")
    private String token;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "用户姓名")
    private String userName;

    @Schema(description = "角色标识")
    private String roleCode;

    @Schema(description = "角色名称")
    private String roleName;
}
