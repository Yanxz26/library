package com.library.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 权限表 sys_permission
 *
 * @author Library Team
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_permission")
public class SysPermission extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 权限名称 */
    private String permName;

    /** 权限标识（如book:add、borrow:list） */
    private String permCode;

    /** 权限类型（1菜单，2接口） */
    private Integer permType;

    /** 父级权限ID */
    private Long parentId;

    // 覆盖基类的updateTime，因为此表不需要更新时间
    @Override
    public LocalDateTime getUpdateTime() {
        return null;
    }
}
