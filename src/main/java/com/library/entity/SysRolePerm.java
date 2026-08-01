package com.library.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 角色权限关联表 sys_role_perm
 *
 * @author Library Team
 */
@Data
@TableName("sys_role_perm")
public class SysRolePerm implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 角色ID */
    private Long roleId;

    /** 权限ID */
    private Long permId;
}
