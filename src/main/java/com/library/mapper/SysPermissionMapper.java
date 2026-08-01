package com.library.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.library.entity.SysPermission;
import org.apache.ibatis.annotations.Mapper;

/**
 * 权限 Mapper
 *
 * @author Library Team
 */
@Mapper
public interface SysPermissionMapper extends BaseMapper<SysPermission> {
}
