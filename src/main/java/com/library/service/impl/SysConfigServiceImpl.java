package com.library.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.common.exception.BusinessException;
import com.library.common.result.PageResult;
import com.library.common.result.Result;
import com.library.common.result.ResultCode;
import com.library.entity.SysConfig;
import com.library.mapper.SysConfigMapper;
import com.library.service.SysConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

/**
 * 系统配置服务实现
 *
 * @author Library Team
 */
@Slf4j
@Service
public class SysConfigServiceImpl implements SysConfigService {

    @Autowired
    private SysConfigMapper sysConfigMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String CONFIG_CACHE_PREFIX = "sys:config:";

    @Override
    public PageResult<SysConfig> pageQuery(Long current, Long size) {
        Page<SysConfig> page = new Page<>(current, size);
        LambdaQueryWrapper<SysConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(SysConfig::getCreateTime);
        Page<SysConfig> result = sysConfigMapper.selectPage(page, wrapper);
        return PageResult.of(result.getCurrent(), result.getSize(), result.getTotal(), result.getRecords());
    }

    @Override
    public String getConfigValue(String configKey) {
        // 先从缓存获取
        String cacheKey = CONFIG_CACHE_PREFIX + configKey;
        Object cachedValue = redisTemplate.opsForValue().get(cacheKey);
        if (cachedValue != null) {
            return cachedValue.toString();
        }

        // 查询数据库
        SysConfig config = sysConfigMapper.selectOne(
                new LambdaQueryWrapper<SysConfig>()
                        .eq(SysConfig::getConfigKey, configKey));
        if (config != null) {
            // 写入缓存
            redisTemplate.opsForValue().set(cacheKey, config.getConfigValue(), 1, TimeUnit.HOURS);
            return config.getConfigValue();
        }

        return null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<?> addConfig(SysConfig config) {
        Long count = sysConfigMapper.selectCount(
                new LambdaQueryWrapper<SysConfig>()
                        .eq(SysConfig::getConfigKey, config.getConfigKey()));
        if (count > 0) {
            throw new BusinessException(ResultCode.CONFIG_KEY_EXIST);
        }
        sysConfigMapper.insert(config);
        log.info("新增系统配置: {} = {}", config.getConfigKey(), config.getConfigValue());
        return Result.ok("新增配置成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<?> updateConfig(SysConfig config) {
        SysConfig existConfig = sysConfigMapper.selectById(config.getId());
        if (existConfig == null) {
            throw new BusinessException(ResultCode.CONFIG_NOT_FOUND);
        }
        sysConfigMapper.updateById(config);

        // 清除缓存
        redisTemplate.delete(CONFIG_CACHE_PREFIX + config.getConfigKey());

        log.info("修改系统配置: id={}, {} = {}", config.getId(), config.getConfigKey(), config.getConfigValue());
        return Result.ok("修改配置成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<?> deleteConfig(Long id) {
        SysConfig config = sysConfigMapper.selectById(id);
        if (config == null) {
            throw new BusinessException(ResultCode.CONFIG_NOT_FOUND);
        }
        sysConfigMapper.deleteById(id);

        // 清除缓存
        redisTemplate.delete(CONFIG_CACHE_PREFIX + config.getConfigKey());

        log.info("删除系统配置: id={}, key={}", id, config.getConfigKey());
        return Result.ok("删除配置成功");
    }
}
