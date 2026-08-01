package com.library.controller;

import com.library.common.annotation.Log;
import com.library.common.result.PageResult;
import com.library.common.result.Result;
import com.library.dto.UserQueryDTO;
import com.library.dto.UserSaveDTO;
import com.library.entity.SysUser;
import com.library.security.SecurityUser;
import com.library.service.SysUserService;
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
 * 用户管理控制器
 *
 * @author Library Team
 */
@Tag(name = "用户管理", description = "用户增删改查、批量导入导出等接口")
@RestController
@RequestMapping("/sys/user")
public class SysUserController {

    @Autowired
    private SysUserService sysUserService;

    @Operation(summary = "分页查询用户列表")
    @PreAuthorize("hasAnyRole('admin', 'library')")
    @GetMapping("/page")
    public Result<PageResult<SysUser>> pageQuery(UserQueryDTO queryDTO) {
        return Result.ok(sysUserService.pageQuery(queryDTO));
    }

    @Operation(summary = "新增用户")
    @Log(type = 1, value = "新增用户")
    @PreAuthorize("hasAnyRole('admin', 'library')")
    @PostMapping("/add")
    public Result<?> addUser(@Valid @RequestBody UserSaveDTO dto) {
        return sysUserService.addUser(dto);
    }

    @Operation(summary = "修改用户")
    @Log(type = 1, value = "修改用户")
    @PreAuthorize("hasAnyRole('admin', 'library')")
    @PostMapping("/update")
    public Result<?> updateUser(@Valid @RequestBody UserSaveDTO dto) {
        return sysUserService.updateUser(dto);
    }

    @Operation(summary = "删除用户")
    @Log(type = 1, value = "删除用户")
    @PreAuthorize("hasRole('admin')")
    @DeleteMapping("/delete/{userId}")
    public Result<?> deleteUser(@Parameter(description = "用户ID") @PathVariable Long userId) {
        return sysUserService.deleteUser(userId);
    }

    @Operation(summary = "修改用户状态（启用/禁用）")
    @Log(type = 1, value = "修改用户状态")
    @PreAuthorize("hasAnyRole('admin', 'library')")
    @PostMapping("/update-status/{userId}/{status}")
    public Result<?> updateStatus(@Parameter(description = "用户ID") @PathVariable Long userId,
                                   @Parameter(description = "状态(0禁用,1正常)") @PathVariable Integer status) {
        return sysUserService.updateStatus(userId, status);
    }

    @Operation(summary = "批量导入用户")
    @Log(type = 1, value = "批量导入用户")
    @PreAuthorize("hasAnyRole('admin', 'library')")
    @PostMapping("/batch-import")
    public Result<?> batchImport(@Parameter(description = "Excel文件") @RequestParam("file") MultipartFile file) {
        return sysUserService.batchImport(file);
    }

    @Operation(summary = "导出用户数据")
    @Log(type = 1, value = "导出用户数据")
    @PreAuthorize("hasAnyRole('admin', 'library')")
    @GetMapping("/export")
    public void exportUsers(UserQueryDTO queryDTO, HttpServletResponse response) {
        sysUserService.exportUsers(queryDTO, response);
    }

    @Operation(summary = "获取用户详情")
    @GetMapping("/detail/{userId}")
    public Result<?> getUserDetail(@Parameter(description = "用户ID") @PathVariable Long userId) {
        return sysUserService.getUserDetail(userId);
    }

    @Operation(summary = "修改个人信息")
    @Log(type = 1, value = "修改个人信息")
    @PostMapping("/profile")
    public Result<?> updateProfile(@AuthenticationPrincipal SecurityUser securityUser,
                                    @RequestBody SysUser profile) {
        return sysUserService.updateProfile(securityUser.getSysUser().getId(), profile);
    }
}
