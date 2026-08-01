package com.library.service;

import com.library.common.result.Result;
import com.library.dto.ChangePasswordDTO;
import com.library.dto.LoginDTO;
import com.library.dto.LoginResultDTO;

/**
 * 认证服务接口
 *
 * @author Library Team
 */
public interface AuthService {

    /**
     * 用户登录
     */
    Result<LoginResultDTO> login(LoginDTO loginDTO, String ipAddr);

    /**
     * 用户登出
     */
    Result<?> logout(String token);

    /**
     * 修改密码
     */
    Result<?> changePassword(Long userId, ChangePasswordDTO dto);

    /**
     * 重置密码
     */
    Result<?> resetPassword(Long userId, Long targetUserId);

    /**
     * 管理员设置用户密码
     */
    Result<?> setPassword(Long userId, Long targetUserId, String newPassword);

    /**
     * 获取当前用户信息
     */
    Result<?> getUserInfo(Long userId);

    /**
     * 忘记密码 - 验证身份
     */
    Result<?> forgotPasswordVerify(String userAccount, String userName);

    /**
     * 忘记密码 - 设置新密码
     */
    Result<?> forgotPasswordReset(String userAccount, String userName, String newPassword);
}
