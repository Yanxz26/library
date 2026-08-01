package com.library.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用户查询DTO
 *
 * @author Library Team
 */
@Data
@Schema(description = "用户查询参数")
public class UserQueryDTO {

    @Schema(description = "页码", example = "1")
    private Long current = 1L;

    @Schema(description = "每页条数", example = "10")
    private Long size = 10L;

    @Schema(description = "账号")
    private String userAccount;

    @Schema(description = "姓名")
    private String userName;

    @Schema(description = "用户类型（1学生，2教师，3管理员）")
    private Integer userType;

    @Schema(description = "角色ID")
    private Long roleId;

    @Schema(description = "账号状态（0禁用，1正常）")
    private Integer status;
}
