package com.library.controller;

import com.library.common.annotation.Log;
import com.library.common.result.PageResult;
import com.library.common.result.Result;
import com.library.dto.BookQueryDTO;
import com.library.dto.BookSaveDTO;
import com.library.entity.BookInfo;
import com.library.security.SecurityUser;
import com.library.service.BookInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

/**
 * 图书信息管理控制器
 *
 * @author Library Team
 */
@Tag(name = "图书信息管理", description = "图书增删改查、下架、批量导入导出等接口")
@RestController
@RequestMapping("/sys/book")
public class BookInfoController {

    @Autowired
    private BookInfoService bookInfoService;

    @Operation(summary = "分页查询图书列表")
    @GetMapping("/page")
    public Result<PageResult<BookInfo>> pageQuery(BookQueryDTO queryDTO) {
        return Result.ok(bookInfoService.pageQuery(queryDTO));
    }

    @Operation(summary = "新增图书")
    @Log(type = 1, value = "新增图书")
    @PreAuthorize("hasAnyRole('admin', 'library')")
    @PostMapping("/add")
    public Result<?> addBook(@Valid @RequestBody BookSaveDTO dto) {
        return bookInfoService.addBook(dto);
    }

    @Operation(summary = "修改图书")
    @Log(type = 1, value = "修改图书信息")
    @PreAuthorize("hasAnyRole('admin', 'library')")
    @PostMapping("/update")
    public Result<?> updateBook(@Valid @RequestBody BookSaveDTO dto) {
        return bookInfoService.updateBook(dto);
    }

    @Operation(summary = "图书下架")
    @Log(type = 1, value = "图书下架")
    @PreAuthorize("hasAnyRole('admin', 'library')")
    @PostMapping("/off-shelf/{bookId}")
    public Result<?> offShelf(@Parameter(description = "图书ID") @PathVariable Long bookId,
                               @AuthenticationPrincipal SecurityUser securityUser) {
        return bookInfoService.offShelf(bookId, securityUser.getSysUser().getId());
    }

    @Operation(summary = "图书上架")
    @Log(type = 1, value = "图书上架")
    @PreAuthorize("hasAnyRole('admin', 'library')")
    @PostMapping("/on-shelf/{bookId}")
    public Result<?> onShelf(@Parameter(description = "图书ID") @PathVariable Long bookId) {
        return bookInfoService.onShelf(bookId);
    }

    @Operation(summary = "删除图书")
    @Log(type = 1, value = "删除图书")
    @PreAuthorize("hasAnyRole('admin', 'library')")
    @DeleteMapping("/delete/{bookId}")
    public Result<?> deleteBook(@Parameter(description = "图书ID") @PathVariable Long bookId) {
        return bookInfoService.deleteBook(bookId);
    }

    @Operation(summary = "库存增补")
    @Log(type = 1, value = "图书库存增补")
    @PreAuthorize("hasAnyRole('admin', 'library')")
    @PostMapping("/add-stock/{bookId}/{addNum}")
    public Result<?> addStock(@Parameter(description = "图书ID") @PathVariable Long bookId,
                               @Parameter(description = "增补数量") @PathVariable Integer addNum) {
        return bookInfoService.addStock(bookId, addNum);
    }

    @Operation(summary = "批量导入图书")
    @Log(type = 1, value = "批量导入图书")
    @PreAuthorize("hasAnyRole('admin', 'library')")
    @PostMapping("/batch-import")
    public Result<?> batchImport(@Parameter(description = "Excel文件") @RequestParam("file") MultipartFile file) {
        return bookInfoService.batchImport(file);
    }

    @Operation(summary = "导出图书数据")
    @Log(type = 1, value = "导出图书数据")
    @PreAuthorize("hasAnyRole('admin', 'library')")
    @GetMapping("/export")
    public void exportBooks(BookQueryDTO queryDTO, HttpServletResponse response) {
        bookInfoService.exportBooks(queryDTO, response);
    }

    @Operation(summary = "获取图书详情")
    @GetMapping("/detail/{bookId}")
    public Result<?> getBookDetail(@Parameter(description = "图书ID") @PathVariable Long bookId) {
        return bookInfoService.getBookDetail(bookId);
    }

    @Operation(summary = "获取热门图书TOP10")
    @GetMapping("/hot")
    public Result<?> getHotBooks() {
        return bookInfoService.getHotBooks();
    }

    @Operation(summary = "获取新书上架")
    @GetMapping("/new")
    public Result<?> getNewBooks() {
        return bookInfoService.getNewBooks();
    }

    @Operation(summary = "上传图书封面")
    @Log(type = 1, value = "上传图书封面")
    @PreAuthorize("hasAnyRole('admin', 'library')")
    @PostMapping("/upload-cover")
    public Result<?> uploadCover(@RequestParam("file") MultipartFile file) {
        return bookInfoService.uploadCover(file);
    }
}
