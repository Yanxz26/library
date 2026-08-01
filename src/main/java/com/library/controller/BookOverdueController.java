package com.library.controller;

import cn.hutool.core.util.StrUtil;
import com.library.common.annotation.Log;
import com.library.common.result.PageResult;
import com.library.common.result.Result;
import com.library.entity.BookOverdue;
import com.library.security.SecurityUser;
import com.library.service.BookOverdueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

/**
 * 逾期管理控制器
 *
 * @author Library Team
 */
@Tag(name = "逾期管理", description = "逾期记录查询、罚款缴纳、减免接口")
@RestController
@RequestMapping("/overdue")
public class BookOverdueController {

    @Autowired
    private BookOverdueService bookOverdueService;

    @Operation(summary = "分页查询逾期记录（管理员）")
    @PreAuthorize("hasAnyRole('admin', 'library')")
    @GetMapping("/page")
    public Result<PageResult<BookOverdue>> pageQuery(
            @Parameter(description = "用户ID(可选)") @RequestParam(required = false) Long userId,
            @Parameter(description = "图书名称(可选)") @RequestParam(required = false) String bookName,
            @Parameter(description = "缴费状态(0未缴费,1已缴费)") @RequestParam(required = false) Integer payStatus,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Long current,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "10") Long size) {
        return Result.ok(bookOverdueService.pageQuery(userId, bookName, payStatus, current, size));
    }

    @Operation(summary = "获取我的逾期记录")
    @GetMapping("/my-overdue")
    public Result<PageResult<BookOverdue>> getMyOverdues(
            @AuthenticationPrincipal SecurityUser securityUser,
            @Parameter(description = "缴费状态(可选)") @RequestParam(required = false) Integer payStatus,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Long current,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "10") Long size) {
        return Result.ok(bookOverdueService.pageQuery(securityUser.getSysUser().getId(), payStatus, current, size));
    }

    @Operation(summary = "缴纳逾期罚款")
    @Log(type = 2, value = "缴纳逾期罚款")
    @PostMapping("/pay/{overdueId}")
    public Result<?> payFine(@AuthenticationPrincipal SecurityUser securityUser,
                              @Parameter(description = "逾期记录ID") @PathVariable Long overdueId) {
        return bookOverdueService.payFine(securityUser.getSysUser().getId(), overdueId);
    }

    @Operation(summary = "管理员减免罚款")
    @Log(type = 1, value = "减免逾期罚款")
    @PreAuthorize("hasAnyRole('admin', 'library')")
    @PostMapping("/waive/{overdueId}")
    public Result<?> waiveFine(@AuthenticationPrincipal SecurityUser securityUser,
                                @Parameter(description = "逾期记录ID") @PathVariable Long overdueId,
                                @Parameter(description = "减免原因") @RequestParam(required = false) String reason) {
        return bookOverdueService.waiveFine(securityUser.getSysUser().getId(), overdueId,
                StrUtil.isNotBlank(reason) ? reason : "管理员手动减免");
    }

    @Operation(summary = "获取逾期汇总信息")
    @GetMapping("/summary")
    public Result<?> getOverdueSummary(@AuthenticationPrincipal SecurityUser securityUser) {
        return bookOverdueService.getUserOverdueSummary(securityUser.getSysUser().getId());
    }

    @Operation(summary = "导出逾期记录")
    @Log(type = 1, value = "导出逾期记录")
    @PreAuthorize("hasAnyRole('admin', 'library')")
    @GetMapping("/export")
    public void exportOverdue(HttpServletResponse response) {
        bookOverdueService.exportOverdue(response);
    }
}
