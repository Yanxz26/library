package com.library.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 角色表 sys_role
 *
 * @author Library Team
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_role")
public class SysRole extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 角色名称 */
    private String roleName;

    /** 角色标识 (admin/library/user) */
    private String roleCode;

    /** 角色状态 (0禁用, 1正常) */
    private Integer status;

    /** 角色描述 */
    private String remark;
}
