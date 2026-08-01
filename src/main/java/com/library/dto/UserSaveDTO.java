package com.library.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 用户新增/修改DTO
 *
 * @author Library Team
 */
@Data
@Schema(description = "用户新增/修改参数")
public class UserSaveDTO {

    @Schema(description = "用户ID（修改时必填）")
    private Long id;

    @NotBlank(message = "账号不能为空")
    @Schema(description = "登录账号（学号/工号）", required = true)
    private String userAccount;

    @NotBlank(message = "姓名不能为空")
    @Schema(description = "用户姓名", required = true)
    private String userName;

    @Schema(description = "密码（新增时必填）")
    private String password;

    @Schema(description = "角色ID（不传则根据用户类型自动设置）")
    private Long roleId;

    @NotNull(message = "用户类型不能为空")
    @Schema(description = "用户类型（1学生，2教师，3管理员）", required = true)
    private Integer userType;

    @Schema(description = "联系电话")
    private String phone;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "最大可借数量")
    private Integer maxBorrow;

    @Schema(description = "账号状态（0禁用，1正常）")
    private Integer status;
}
