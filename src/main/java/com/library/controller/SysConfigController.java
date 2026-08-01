package com.library.controller;

import com.library.common.annotation.Log;
import com.library.common.result.PageResult;
import com.library.common.result.Result;
import com.library.entity.SysConfig;
import com.library.service.SysConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 系统配置控制器
 *
 * @author Library Team
 */
@Tag(name = "系统配置", description = "系统参数动态配置接口")
@RestController
@RequestMapping("/sys/config")
@PreAuthorize("hasAnyRole('admin', 'library')")
public class SysConfigController {

    @Autowired
    private SysConfigService sysConfigService;

    @Operation(summary = "分页查询配置")
    @GetMapping("/page")
    public Result<PageResult<SysConfig>> pageQuery(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Long current,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "10") Long size) {
        return Result.ok(sysConfigService.pageQuery(current, size));
    }

    @Operation(summary = "根据键获取配置值")
    @GetMapping("/value/{configKey}")
    public Result<String> getConfigValue(
            @Parameter(description = "配置键名") @PathVariable String configKey) {
        return Result.ok(sysConfigService.getConfigValue(configKey));
    }

    @Operation(summary = "新增配置")
    @Log(type = 1, value = "新增系统配置")
    @PreAuthorize("hasRole('admin')")
    @PostMapping("/add")
    public Result<?> addConfig(@Valid @RequestBody SysConfig config) {
        return sysConfigService.addConfig(config);
    }

    @Operation(summary = "修改配置")
    @Log(type = 1, value = "修改系统配置")
    @PreAuthorize("hasRole('admin')")
    @PostMapping("/update")
    public Result<?> updateConfig(@Valid @RequestBody SysConfig config) {
        return sysConfigService.updateConfig(config);
    }

    @Operation(summary = "删除配置")
    @Log(type = 1, value = "删除系统配置")
    @PreAuthorize("hasRole('admin')")
    @DeleteMapping("/delete/{id}")
    public Result<?> deleteConfig(@Parameter(description = "配置ID") @PathVariable Long id) {
        return sysConfigService.deleteConfig(id);
    }

    @Operation(summary = "获取所有配置列表（公开）")
    @GetMapping("/public/list")
    public Result<PageResult<SysConfig>> getPublicConfigs() {
        return Result.ok(sysConfigService.pageQuery(1L, 100L));
    }
}
