package com.library.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.library.common.exception.BusinessException;
import com.library.common.result.Result;
import com.library.common.result.ResultCode;
import com.library.common.utils.JwtUtil;
import com.library.dto.ChangePasswordDTO;
import com.library.dto.LoginDTO;
import com.library.dto.LoginResultDTO;
import com.library.entity.*;
import com.library.mapper.*;
import com.library.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 认证服务实现
 *
 * @author Library Team
 */
@Slf4j
@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private SysRoleMapper sysRoleMapper;

    @Autowired
    private BookBorrowMapper bookBorrowMapper;

    @Autowired
    private BookOverdueMapper bookOverdueMapper;

    @Autowired
    private BookReserveMapper bookReserveMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public Result<LoginResultDTO> login(LoginDTO loginDTO, String ipAddr) {
        // 查询用户
        SysUser user = sysUserMapper.selectOne(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getUserAccount, loginDTO.getUserAccount())
                        .eq(SysUser::getIsDelete, 0));

        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        // 检查账号状态
        if (user.getStatus() == 0) {
            throw new BusinessException(ResultCode.USER_DISABLED);
        }

        // 验证密码（明文对比）
        if (!loginDTO.getPassword().equals(user.getPassword())) {
            throw new BusinessException(ResultCode.PASSWORD_ERROR);
        }

        // 获取角色信息
        SysRole role = sysRoleMapper.selectById(user.getRoleId());
        String roleCode = role != null ? role.getRoleCode() : "user";
        String roleName = role != null ? role.getRoleName() : "普通用户";
        
        // 根据用户类型调整角色码和角色名
        if (user.getUserType() != null) {
            if (user.getUserType() == 2) {
                roleCode = "teacher";
                roleName = "教师";
            } else if (user.getUserType() == 1) {
                roleCode = "user";
                roleName = "学生";
            }
        }

        // 生成Token
        String token = jwtUtil.generateToken(user.getId(), user.getUserAccount(), roleCode);

        // Token存入Redis（用于登出控制）
        try {
            redisTemplate.opsForValue().set("token:user:" + user.getId(), token, 86400, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("Redis连接异常，Token未缓存: {}", e.getMessage());
        }

        // 记录登录日志
        log.info("用户登录成功: userId={}, account={}, ip={}", user.getId(), user.getUserAccount(), ipAddr);

        // 构建返回数据
        LoginResultDTO resultDTO = LoginResultDTO.builder()
                .token(token)
                .userId(user.getId())
                .userName(user.getUserName())
                .roleCode(roleCode)
                .roleName(roleName)
                .build();

        return Result.ok("登录成功", resultDTO);
    }

    @Override
    public Result<?> logout(String token) {
        if (StrUtil.isNotBlank(token)) {
            try {
                redisTemplate.opsForValue().set("token:blacklist:" + token, "1", 86400, TimeUnit.SECONDS);
                Long userId = jwtUtil.getUserId(token);
                if (userId != null) {
                    redisTemplate.delete("token:user:" + userId);
                }
            } catch (Exception e) {
                log.warn("Redis连接异常，登出操作部分失败: {}", e.getMessage());
            }
        }
        return Result.ok("登出成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<?> changePassword(Long userId, ChangePasswordDTO dto) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        // 验证原密码（明文对比）
        if (!dto.getOldPassword().equals(user.getPassword())) {
            throw new BusinessException(ResultCode.OLD_PASSWORD_ERROR);
        }

        // 更新密码（明文存储）
        user.setPassword(dto.getNewPassword());
        sysUserMapper.updateById(user);

        log.info("用户修改密码成功: userId={}", userId);
        return Result.ok("密码修改成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<?> resetPassword(Long userId, Long targetUserId) {
        SysUser targetUser = sysUserMapper.selectById(targetUserId);
        if (targetUser == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        // 重置为默认密码（账号后6位，明文存储）
        String account = targetUser.getUserAccount();
        String defaultPwd = account.length() >= 6 ? account.substring(account.length() - 6) : account;
        targetUser.setPassword(defaultPwd);
        sysUserMapper.updateById(targetUser);

        log.info("管理员{}重置用户{}密码", userId, targetUserId);
        return Result.ok("密码已重置为默认密码");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<?> setPassword(Long userId, Long targetUserId, String newPassword) {
        SysUser targetUser = sysUserMapper.selectById(targetUserId);
        if (targetUser == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        if (StrUtil.isBlank(newPassword) || newPassword.length() < 6) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "密码长度不能少于6位");
        }

        // 设置新密码（明文存储）
        targetUser.setPassword(newPassword);
        sysUserMapper.updateById(targetUser);

        log.info("管理员{}设置用户{}的新密码", userId, targetUserId);
        return Result.ok("密码设置成功");
    }

    @Override
    public Result<?> getUserInfo(Long userId) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        SysRole role = sysRoleMapper.selectById(user.getRoleId());

        // 统计当前借阅数量
        Long borrowCount = bookBorrowMapper.selectCount(
                new LambdaQueryWrapper<BookBorrow>()
                        .eq(BookBorrow::getUserId, userId)
                        .eq(BookBorrow::getBorrowStatus, 1));

        // 统计逾期数量
        Long overdueCount = bookOverdueMapper.selectCount(
                new LambdaQueryWrapper<BookOverdue>()
                        .eq(BookOverdue::getUserId, userId)
                        .eq(BookOverdue::getPayStatus, 0));

        // 统计预约数量
        Long reserveCount = bookReserveMapper.selectCount(
                new LambdaQueryWrapper<BookReserve>()
                        .eq(BookReserve::getUserId, userId)
                        .eq(BookReserve::getReserveStatus, 1));

        String roleCode = role != null ? role.getRoleCode() : "user";
        String roleName = role != null ? role.getRoleName() : "普通用户";
        
        if (user.getUserType() != null) {
            if (user.getUserType() == 2) {
                roleCode = "teacher";
                roleName = "教师";
            } else if (user.getUserType() == 1) {
                roleCode = "user";
                roleName = "学生";
            }
        }

        Map<String, Object> data = new HashMap<>();
        data.put("userId", user.getId());
        data.put("userAccount", user.getUserAccount());
        data.put("userName", user.getUserName());
        data.put("userType", user.getUserType());
        data.put("phone", user.getPhone());
        data.put("email", user.getEmail());
        data.put("avatar", user.getAvatar());
        data.put("roleId", user.getRoleId());
        data.put("roleName", roleName);
        data.put("roleCode", roleCode);
        data.put("borrowNum", user.getBorrowNum());
        data.put("maxBorrow", user.getMaxBorrow());
        data.put("currentBorrowCount", borrowCount);
        data.put("overdueCount", overdueCount);
        data.put("reserveCount", reserveCount);
        data.put("remainingBorrow", user.getMaxBorrow() - user.getBorrowNum());

        return Result.ok(data);
    }

    @Override
    public Result<?> forgotPasswordVerify(String userAccount, String userName) {
        if (StrUtil.isBlank(userAccount) || StrUtil.isBlank(userName)) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "账号和用户名不能为空");
        }

        SysUser user = sysUserMapper.selectOne(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getUserAccount, userAccount)
                        .eq(SysUser::getUserName, userName)
                        .eq(SysUser::getIsDelete, 0));

        if (user == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "账号或用户名不正确");
        }

        if (user.getStatus() == 0) {
            throw new BusinessException(ResultCode.USER_DISABLED);
        }

        log.info("忘记密码身份验证成功: account={}", userAccount);
        return Result.ok("身份验证成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<?> forgotPasswordReset(String userAccount, String userName, String newPassword) {
        if (StrUtil.isBlank(userAccount) || StrUtil.isBlank(userName)) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "账号和用户名不能为空");
        }

        if (StrUtil.isBlank(newPassword) || newPassword.length() < 6) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "密码长度不能少于6位");
        }

        SysUser user = sysUserMapper.selectOne(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getUserAccount, userAccount)
                        .eq(SysUser::getUserName, userName)
                        .eq(SysUser::getIsDelete, 0));

        if (user == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "账号或用户名不正确");
        }

        if (user.getStatus() == 0) {
            throw new BusinessException(ResultCode.USER_DISABLED);
        }

        user.setPassword(newPassword);
        sysUserMapper.updateById(user);

        log.info("用户通过忘记密码修改密码: account={}", userAccount);
        return Result.ok("密码修改成功，请重新登录");
    }
}
