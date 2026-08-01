package com.library.service;

import com.library.common.result.PageResult;
import com.library.common.result.Result;
import com.library.entity.SysConfig;

/**
 * 系统配置服务接口
 *
 * @author Library Team
 */
public interface SysConfigService {

    /**
     * 分页查询配置
     */
    PageResult<SysConfig> pageQuery(Long current, Long size);

    /**
     * 根据键获取配置值
     */
    String getConfigValue(String configKey);

    /**
     * 新增配置
     */
    Result<?> addConfig(SysConfig config);

    /**
     * 修改配置
     */
    Result<?> updateConfig(SysConfig config);

    /**
     * 删除配置
     */
    Result<?> deleteConfig(Long id);
}
