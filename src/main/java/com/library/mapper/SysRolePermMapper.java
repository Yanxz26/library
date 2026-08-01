package com.library.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.library.entity.SysRolePerm;
import org.apache.ibatis.annotations.Mapper;

/**
 * 角色权限关联 Mapper
 *
 * @author Library Team
 */
@Mapper
public interface SysRolePermMapper extends BaseMapper<SysRolePerm> {
}
