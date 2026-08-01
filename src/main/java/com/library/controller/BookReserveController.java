package com.library.controller;

import com.library.common.annotation.Log;
import com.library.common.result.PageResult;
import com.library.common.result.Result;
import com.library.entity.BookReserve;
import com.library.security.SecurityUser;
import com.library.service.BookReserveService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 图书预约控制器
 *
 * @author Library Team
 */
@Tag(name = "图书预约管理", description = "图书预约、取消预约接口")
@RestController
@RequestMapping("/reserve")
public class BookReserveController {

    @Autowired
    private BookReserveService bookReserveService;

    @Operation(summary = "图书预约")
    @Log(type = 2, value = "图书预约")
    @PostMapping("/{bookId}")
    public Result<?> reserve(@AuthenticationPrincipal SecurityUser securityUser,
                              @Parameter(description = "图书ID") @PathVariable Long bookId) {
        return bookReserveService.reserve(securityUser.getSysUser().getId(), bookId);
    }

    @Operation(summary = "取消预约")
    @Log(type = 2, value = "取消预约")
    @PostMapping("/cancel/{reserveId}")
    public Result<?> cancelReserve(@AuthenticationPrincipal SecurityUser securityUser,
                                    @Parameter(description = "预约ID") @PathVariable Long reserveId) {
        return bookReserveService.cancelReserve(securityUser.getSysUser().getId(), reserveId);
    }

    @Operation(summary = "分页查询预约记录(管理员)")
    @PreAuthorize("hasAnyRole('admin', 'library')")
    @GetMapping("/page")
    public Result<PageResult<BookReserve>> pageQuery(
            @Parameter(description = "用户ID(可选)")
            @RequestParam(required = false) Long userId,
            @Parameter(description = "图书名称(可选)")
            @RequestParam(required = false) String bookName,
            @Parameter(description = "状态(可选)")
            @RequestParam(required = false) Integer reserveStatus,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Long current,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "10") Long size) {
        return Result.ok(bookReserveService.pageQuery(userId, bookName, reserveStatus, current, size));
    }

    @Operation(summary = "标记预约完成(管理员)")
    @PreAuthorize("hasAnyRole('admin', 'library')")
    @PostMapping("/complete/{reserveId}")
    public Result<?> completeReserve(@Parameter(description = "预约ID") @PathVariable Long reserveId) {
        return bookReserveService.completeReserve(reserveId);
    }

    @Operation(summary = "管理员替用户预约")
    @PreAuthorize("hasAnyRole('admin', 'library')")
    @PostMapping("/admin/{bookId}")
    public Result<?> adminReserve(@Parameter(description = "用户ID") @RequestParam Long userId,
                                   @Parameter(description = "图书ID") @PathVariable Long bookId) {
        return bookReserveService.reserve(userId, bookId);
    }

    @Operation(summary = "获取我的预约记录")
    @GetMapping("/my-reserves")
    public Result<PageResult<BookReserve>> getMyReserves(@AuthenticationPrincipal SecurityUser securityUser,
                                                           @Parameter(description = "页码") @RequestParam(defaultValue = "1") Long current,
                                                           @Parameter(description = "每页条数") @RequestParam(defaultValue = "10") Long size) {
        return Result.ok(bookReserveService.pageQuery(securityUser.getSysUser().getId(), current, size));
    }
}
