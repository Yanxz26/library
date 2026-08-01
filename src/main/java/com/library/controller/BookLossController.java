package com.library.controller;

import com.library.common.annotation.Log;
import com.library.common.result.PageResult;
import com.library.common.result.Result;
import com.library.dto.LossSaveDTO;
import com.library.entity.BookLoss;
import com.library.security.SecurityUser;
import com.library.service.BookLossService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

/**
 * 图书损耗控制器
 *
 * @author Library Team
 */
@Tag(name = "图书损耗管理", description = "损耗登记、损耗记录查询导出接口")
@RestController
@RequestMapping("/sys/loss")
@PreAuthorize("hasAnyRole('admin', 'library')")
public class BookLossController {

    @Autowired
    private BookLossService bookLossService;

    @Operation(summary = "分页查询损耗记录")
    @GetMapping("/page")
    public Result<PageResult<BookLoss>> pageQuery(
            @Parameter(description = "图书名称(可选)") @RequestParam(required = false) String bookName,
            @Parameter(description = "损耗类型(可选)") @RequestParam(required = false) Integer lossType,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Long current,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "10") Long size) {
        return Result.ok(bookLossService.pageQuery(bookName, lossType, current, size));
    }

    @Operation(summary = "登记损耗")
    @Log(type = 1, value = "登记图书损耗")
    @PostMapping("/register")
    public Result<?> registerLoss(@AuthenticationPrincipal SecurityUser securityUser,
                                   @Valid @RequestBody LossSaveDTO dto) {
        return bookLossService.registerLoss(securityUser.getSysUser().getId(), dto);
    }

    @Operation(summary = "导出损耗记录")
    @Log(type = 1, value = "导出损耗记录")
    @GetMapping("/export")
    public void exportLoss(HttpServletResponse response) {
        bookLossService.exportLoss(response);
    }
}
