package com.library.controller;

import com.library.common.annotation.Log;
import com.library.common.result.PageResult;
import com.library.common.result.Result;
import com.library.dto.BorrowQueryDTO;
import com.library.entity.BookBorrow;
import com.library.security.SecurityUser;
import com.library.service.BookBorrowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

/**
 * 图书借阅控制器
 *
 * @author Library Team
 */
@Tag(name = "图书借阅管理", description = "借阅、归还、续借等核心业务接口")
@RestController
@RequestMapping("/borrow")
public class BookBorrowController {

    @Autowired
    private BookBorrowService bookBorrowService;

    @Operation(summary = "图书借阅")
    @Log(type = 2, value = "图书借阅")
    @PostMapping("/{bookId}")
    public Result<?> borrow(@AuthenticationPrincipal SecurityUser securityUser,
                             @Parameter(description = "图书ID") @PathVariable Long bookId) {
        return bookBorrowService.borrow(securityUser.getSysUser().getId(), bookId);
    }

    @Operation(summary = "图书归还")
    @Log(type = 2, value = "图书归还")
    @PostMapping("/return/{borrowId}")
    public Result<?> returnBook(@AuthenticationPrincipal SecurityUser securityUser,
                                 @Parameter(description = "借阅订单ID") @PathVariable Long borrowId) {
        return bookBorrowService.returnBook(securityUser.getSysUser().getId(), borrowId);
    }

    @Operation(summary = "图书续借")
    @Log(type = 2, value = "图书续借")
    @PostMapping("/renew/{borrowId}")
    public Result<?> renew(@AuthenticationPrincipal SecurityUser securityUser,
                            @Parameter(description = "借阅订单ID") @PathVariable Long borrowId) {
        return bookBorrowService.renew(securityUser.getSysUser().getId(), borrowId);
    }

    @Operation(summary = "管理员线下登记归还")
    @Log(type = 1, value = "管理员登记归还")
    @PreAuthorize("hasAnyRole('admin', 'library')")
    @PostMapping("/admin-return/{borrowId}")
    public Result<?> adminReturn(@AuthenticationPrincipal SecurityUser securityUser,
                                  @Parameter(description = "借阅订单ID") @PathVariable Long borrowId) {
        return bookBorrowService.adminReturn(securityUser.getSysUser().getId(), borrowId);
    }

    @Operation(summary = "分页查询借阅记录（管理员）")
    @PreAuthorize("hasAnyRole('admin', 'library')")
    @GetMapping("/page")
    public Result<PageResult<BookBorrow>> pageQuery(BorrowQueryDTO queryDTO) {
        return Result.ok(bookBorrowService.pageQuery(queryDTO));
    }

    @Operation(summary = "获取当前用户借阅记录")
    @GetMapping("/my-borrows")
    public Result<PageResult<BookBorrow>> getUserBorrows(@AuthenticationPrincipal SecurityUser securityUser,
                                                           BorrowQueryDTO queryDTO) {
        return Result.ok(bookBorrowService.getUserBorrows(securityUser.getSysUser().getId(), queryDTO));
    }

    @Operation(summary = "导出借阅记录")
    @Log(type = 1, value = "导出借阅记录")
    @PreAuthorize("hasAnyRole('admin', 'library')")
    @GetMapping("/export")
    public void exportBorrows(BorrowQueryDTO queryDTO, HttpServletResponse response) {
        bookBorrowService.exportBorrows(queryDTO, response);
    }
}
