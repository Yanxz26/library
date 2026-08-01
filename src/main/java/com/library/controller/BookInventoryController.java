package com.library.controller;

import com.library.common.annotation.Log;
import com.library.common.result.PageResult;
import com.library.common.result.Result;
import com.library.dto.InventorySaveDTO;
import com.library.entity.BookInventory;
import com.library.security.SecurityUser;
import com.library.service.BookInventoryService;
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
 * 图书盘点控制器
 *
 * @author Library Team
 */
@Tag(name = "图书盘点管理", description = "库存盘点、盘点记录导出接口")
@RestController
@RequestMapping("/sys/inventory")
@PreAuthorize("hasAnyRole('admin', 'library')")
public class BookInventoryController {

    @Autowired
    private BookInventoryService bookInventoryService;

    @Operation(summary = "分页查询盘点记录")
    @GetMapping("/page")
    public Result<PageResult<BookInventory>> pageQuery(
            @Parameter(description = "图书名称(可选)") @RequestParam(required = false) String bookName,
            @Parameter(description = "是否有差异(可选)") @RequestParam(required = false) Boolean hasDiff,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Long current,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "10") Long size) {
        return Result.ok(bookInventoryService.pageQuery(bookName, hasDiff, current, size));
    }

    @Operation(summary = "执行盘点")
    @Log(type = 1, value = "图书盘点")
    @PostMapping("/do")
    public Result<?> doInventory(@AuthenticationPrincipal SecurityUser securityUser,
                                  @Valid @RequestBody InventorySaveDTO dto) {
        return bookInventoryService.doInventory(securityUser.getSysUser().getId(), dto);
    }

    @Operation(summary = "导出盘点记录")
    @Log(type = 1, value = "导出盘点记录")
    @GetMapping("/export")
    public void exportInventory(HttpServletResponse response) {
        bookInventoryService.exportInventory(response);
    }
}
