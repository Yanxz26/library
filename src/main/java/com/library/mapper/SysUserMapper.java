package com.library.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.library.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户 Mapper
 *
 * @author Library Team
 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {
}
