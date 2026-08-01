package com.library.controller;

import com.library.common.annotation.Log;
import com.library.common.result.Result;
import com.library.common.utils.IpUtil;
import com.library.dto.ChangePasswordDTO;
import com.library.dto.LoginDTO;
import com.library.dto.LoginResultDTO;
import com.library.security.SecurityUser;
import com.library.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

/**
 * 认证控制器
 *
 * @author Library Team
 */
@Tag(name = "认证管理", description = "登录、登出、密码修改等接口")
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<LoginResultDTO> login(@Valid @RequestBody LoginDTO loginDTO, HttpServletRequest request) {
        String ipAddr = IpUtil.getIpAddr(request);
        return authService.login(loginDTO, ipAddr);
    }

    @Operation(summary = "用户登出")
    @PostMapping("/logout")
    public Result<?> logout(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        return authService.logout(token);
    }

    @Operation(summary = "修改密码")
    @Log(type = 1, value = "修改密码")
    @PostMapping("/change-password")
    public Result<?> changePassword(@AuthenticationPrincipal SecurityUser securityUser,
                                     @Valid @RequestBody ChangePasswordDTO dto) {
        return authService.changePassword(securityUser.getSysUser().getId(), dto);
    }

    @Operation(summary = "管理员重置用户密码")
    @Log(type = 1, value = "重置用户密码")
    @PreAuthorize("hasAnyRole('admin', 'library')")
    @PostMapping("/reset-password/{userId}")
    public Result<?> resetPassword(@AuthenticationPrincipal SecurityUser securityUser,
                                    @Parameter(description = "目标用户ID") @PathVariable Long userId) {
        return authService.resetPassword(securityUser.getSysUser().getId(), userId);
    }

    @Operation(summary = "管理员设置用户密码")
    @Log(type = 1, value = "设置用户密码")
    @PreAuthorize("hasAnyRole('admin', 'library')")
    @PostMapping("/set-password/{userId}")
    public Result<?> setPassword(@AuthenticationPrincipal SecurityUser securityUser,
                                  @Parameter(description = "目标用户ID") @PathVariable Long userId,
                                  @Parameter(description = "新密码") @RequestBody java.util.Map<String, String> body) {
        return authService.setPassword(securityUser.getSysUser().getId(), userId, body.get("password"));
    }

    @Operation(summary = "获取当前用户信息")
    @GetMapping("/user-info")
    public Result<?> getUserInfo(@AuthenticationPrincipal SecurityUser securityUser) {
        return authService.getUserInfo(securityUser.getSysUser().getId());
    }

    @Operation(summary = "忘记密码 - 验证身份")
    @PostMapping("/forgot-password/verify")
    public Result<?> forgotPasswordVerify(@RequestBody java.util.Map<String, String> body) {
        return authService.forgotPasswordVerify(body.get("userAccount"), body.get("userName"));
    }

    @Operation(summary = "忘记密码 - 设置新密码")
    @PostMapping("/forgot-password/reset")
    public Result<?> forgotPasswordReset(@RequestBody java.util.Map<String, String> body) {
        return authService.forgotPasswordReset(
                body.get("userAccount"), 
                body.get("userName"), 
                body.get("newPassword"));
    }
}
