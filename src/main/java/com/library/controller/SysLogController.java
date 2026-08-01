package com.library.controller;

import com.library.common.annotation.Log;
import com.library.common.result.PageResult;
import com.library.common.result.Result;
import com.library.entity.SysLog;
import com.library.service.SysLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

/**
 * 系统日志控制器
 *
 * @author Library Team
 */
@Tag(name = "系统日志", description = "操作日志、业务日志、异常日志查询导出接口")
@RestController
@RequestMapping("/sys/log")
@PreAuthorize("hasAnyRole('admin', 'library')")
public class SysLogController {

    @Autowired
    private SysLogService sysLogService;

    @Operation(summary = "分页查询日志")
    @GetMapping("/page")
    public Result<PageResult<SysLog>> pageQuery(
            @Parameter(description = "日志类型(1操作日志,2业务日志,3异常日志)")
            @RequestParam(required = false) Integer logType,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Long current,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "20") Long size) {
        return Result.ok(sysLogService.pageQuery(logType, current, size));
    }

    @Operation(summary = "查询操作日志")
    @GetMapping("/operation")
    public Result<PageResult<SysLog>> getOperationLogs(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Long current,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "20") Long size) {
        return Result.ok(sysLogService.pageQuery(1, current, size));
    }

    @Operation(summary = "查询业务日志")
    @GetMapping("/business")
    public Result<PageResult<SysLog>> getBusinessLogs(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Long current,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "20") Long size) {
        return Result.ok(sysLogService.pageQuery(2, current, size));
    }

    @Operation(summary = "查询异常日志")
    @GetMapping("/error")
    public Result<PageResult<SysLog>> getErrorLogs(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Long current,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "20") Long size) {
        return Result.ok(sysLogService.pageQuery(3, current, size));
    }

    @Operation(summary = "导出日志")
    @Log(type = 1, value = "导出系统日志")
    @GetMapping("/export")
    public void exportLogs(@Parameter(description = "日志类型") @RequestParam(required = false) Integer logType,
                           HttpServletResponse response) {
        sysLogService.exportLogs(logType, response);
    }
}
