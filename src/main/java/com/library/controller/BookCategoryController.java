package com.library.controller;

import com.library.common.annotation.Log;
import com.library.common.result.Result;
import com.library.entity.BookCategory;
import com.library.service.BookCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 图书分类控制器
 *
 * @author Library Team
 */
@Tag(name = "图书分类管理", description = "图书分类增删改查接口")
@RestController
@RequestMapping("/sys/category")
public class BookCategoryController {

    @Autowired
    private BookCategoryService bookCategoryService;

    @Operation(summary = "获取分类树")
    @GetMapping("/tree")
    public Result<List<BookCategory>> getCategoryTree() {
        return Result.ok(bookCategoryService.getCategoryTree());
    }

    @Operation(summary = "新增分类")
    @Log(type = 1, value = "新增图书分类")
    @PreAuthorize("hasAnyRole('admin', 'library')")
    @PostMapping("/add")
    public Result<?> addCategory(@RequestBody BookCategory category) {
        return bookCategoryService.addCategory(category);
    }

    @Operation(summary = "修改分类")
    @Log(type = 1, value = "修改图书分类")
    @PreAuthorize("hasAnyRole('admin', 'library')")
    @PostMapping("/update")
    public Result<?> updateCategory(@RequestBody BookCategory category) {
        return bookCategoryService.updateCategory(category);
    }

    @Operation(summary = "删除分类")
    @Log(type = 1, value = "删除图书分类")
    @PreAuthorize("hasAnyRole('admin', 'library')")
    @DeleteMapping("/delete/{id}")
    public Result<?> deleteCategory(@Parameter(description = "分类ID") @PathVariable Long id) {
        return bookCategoryService.deleteCategory(id);
    }

    @Operation(summary = "更新分类状态")
    @Log(type = 1, value = "更新分类状态")
    @PreAuthorize("hasAnyRole('admin', 'library')")
    @PostMapping("/update-status/{id}/{status}")
    public Result<?> updateStatus(@Parameter(description = "分类ID") @PathVariable Long id,
                                   @Parameter(description = "状态(0禁用,1正常)") @PathVariable Integer status) {
        return bookCategoryService.updateStatus(id, status);
    }
}
