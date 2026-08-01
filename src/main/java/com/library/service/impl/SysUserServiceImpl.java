package com.library.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.library.common.exception.BusinessException;
import com.library.common.result.PageResult;
import com.library.common.result.Result;
import com.library.common.result.ResultCode;
import com.library.common.utils.ExcelUtil;
import com.library.common.utils.Md5Util;
import com.library.dto.UserQueryDTO;
import com.library.dto.UserSaveDTO;
import com.library.entity.BookOverdue;
import com.library.entity.SysUser;
import com.library.mapper.BookOverdueMapper;
import com.library.mapper.SysUserMapper;
import com.library.service.SysUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * 用户管理服务实现
 *
 * @author Library Team
 */
@Slf4j
@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private BookOverdueMapper bookOverdueMapper;

    @Override
    public PageResult<SysUser> pageQuery(UserQueryDTO queryDTO) {
        Page<SysUser> page = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();

        if (StrUtil.isNotBlank(queryDTO.getUserAccount())) {
            wrapper.like(SysUser::getUserAccount, queryDTO.getUserAccount());
        }
        if (StrUtil.isNotBlank(queryDTO.getUserName())) {
            wrapper.like(SysUser::getUserName, queryDTO.getUserName());
        }
        if (queryDTO.getUserType() != null) {
            wrapper.eq(SysUser::getUserType, queryDTO.getUserType());
        }
        if (queryDTO.getRoleId() != null) {
            wrapper.eq(SysUser::getRoleId, queryDTO.getRoleId());
        }
        if (queryDTO.getStatus() != null) {
            wrapper.eq(SysUser::getStatus, queryDTO.getStatus());
        }

        wrapper.orderByDesc(SysUser::getCreateTime);
        Page<SysUser> result = sysUserMapper.selectPage(page, wrapper);

        return PageResult.of(result.getCurrent(), result.getSize(),
                result.getTotal(), result.getRecords());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<?> addUser(UserSaveDTO dto) {
        // 检查账号是否已存在
        Long count = sysUserMapper.selectCount(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getUserAccount, dto.getUserAccount()));
        if (count > 0) {
            throw new BusinessException(ResultCode.USER_ACCOUNT_EXIST);
        }

        SysUser user = new SysUser();
        BeanUtils.copyProperties(dto, user);

        // 密码处理
        if (StrUtil.isNotBlank(dto.getPassword())) {
            user.setPassword(Md5Util.encode(dto.getPassword()));
        } else {
            user.setPassword(Md5Util.generateDefaultPassword(dto.getUserAccount()));
        }

        // 默认值
        if (user.getRoleId() == null) {
            if (dto.getUserType() == 3) {
                user.setRoleId(1L);
            } else {
                user.setRoleId(3L);
            }
        }
        user.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        user.setBorrowNum(0);
        if (user.getMaxBorrow() == null) {
            // 学生默认10本，教师默认20本
            user.setMaxBorrow(dto.getUserType() == 2 ? 20 : 10);
        }
        if (user.getAvatar() == null) {
            user.setAvatar("default-avatar.png");
        }

        sysUserMapper.insert(user);
        log.info("新增用户成功: account={}, name={}", user.getUserAccount(), user.getUserName());
        return Result.ok("新增用户成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<?> updateUser(UserSaveDTO dto) {
        SysUser user = sysUserMapper.selectById(dto.getId());
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        BeanUtils.copyProperties(dto, user);
        // 不覆盖密码（密码通过独立接口修改）
        user.setPassword(null);

        sysUserMapper.updateById(user);
        log.info("修改用户成功: id={}, account={}", user.getId(), user.getUserAccount());
        return Result.ok("修改用户成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<?> deleteUser(Long userId) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        // 逻辑删除
        sysUserMapper.deleteById(userId);
        log.info("删除用户成功: id={}", userId);
        return Result.ok("删除用户成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<?> updateStatus(Long userId, Integer status) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        user.setStatus(status);
        sysUserMapper.updateById(user);
        log.info("用户状态更新: id={}, status={}", userId, status);
        return Result.ok(status == 1 ? "账号已启用" : "账号已禁用");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<?> batchImport(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessException(ResultCode.FILE_UPLOAD_ERROR, "文件不能为空");
        }

        try {
            List<List<String>> rows = ExcelUtil.read(file.getInputStream());
            if (rows.size() < 2) {
                throw new BusinessException(ResultCode.FILE_FORMAT_ERROR, "Excel文件无数据");
            }

            // 跳过表头，从第二行开始读取
            int successCount = 0;
            List<String> errorList = new ArrayList<>();

            for (int i = 1; i < rows.size(); i++) {
                try {
                    List<String> row = rows.get(i);
                    if (row.size() < 4) {
                        errorList.add("第" + (i + 1) + "行: 数据不完整");
                        continue;
                    }

                    String userAccount = row.get(0).trim();
                    String userName = row.get(1).trim();
                    String userTypeStr = row.get(2).trim();
                    String roleIdStr = row.get(3).trim();

                    if (StrUtil.isBlank(userAccount) || StrUtil.isBlank(userName)) {
                        errorList.add("第" + (i + 1) + "行: 账号或姓名为空");
                        continue;
                    }

                    // 检查账号是否已存在
                    Long count = sysUserMapper.selectCount(
                            new LambdaQueryWrapper<SysUser>()
                                    .eq(SysUser::getUserAccount, userAccount));
                    if (count > 0) {
                        errorList.add("第" + (i + 1) + "行: 账号已存在");
                        continue;
                    }

                    SysUser user = new SysUser();
                    user.setUserAccount(userAccount);
                    user.setUserName(userName);
                    user.setUserType("2".equals(userTypeStr) ? 2 : "3".equals(userTypeStr) ? 3 : 1);
                    user.setRoleId("2".equals(roleIdStr) ? 2L : "3".equals(roleIdStr) ? 3L : 1L);
                    user.setPassword(Md5Util.generateDefaultPassword(userAccount));
                    user.setMaxBorrow(user.getUserType() == 2 ? 20 : 10);
                    user.setBorrowNum(0);
                    user.setStatus(1);
                    user.setAvatar("default-avatar.png");

                    sysUserMapper.insert(user);
                    successCount++;
                } catch (Exception e) {
                    errorList.add("第" + (i + 1) + "行: " + e.getMessage());
                }
            }

            String msg = "导入完成，成功" + successCount + "条";
            if (!errorList.isEmpty()) {
                msg += "，失败" + errorList.size() + "条: " + String.join("; ", errorList);
            }
            return Result.ok(msg);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("批量导入用户失败", e);
            throw new BusinessException(ResultCode.FILE_FORMAT_ERROR, "请检查Excel格式");
        }
    }

    @Override
    public void exportUsers(UserQueryDTO queryDTO, HttpServletResponse response) {
        PageResult<SysUser> pageResult = pageQuery(queryDTO);
        List<SysUser> userList = pageResult.getRecords();

        String[] headers = {"账号", "姓名", "用户类型", "联系电话", "邮箱", "已借数量", "最大可借", "状态", "创建时间"};
        String[] fields = {"userAccount", "userName", "userType", "phone", "email",
                "borrowNum", "maxBorrow", "status", "createTime"};

        // 转换用户类型和状态
        userList.forEach(user -> {
            if (user.getUserType() != null) {
                if (user.getUserType() == 1) user.setEmail("学生");
                else if (user.getUserType() == 2) user.setEmail("教师");
                else if (user.getUserType() == 3) user.setEmail("管理员");
            }
        });

        ExcelUtil.export(response, "用户数据", userList, headers, fields);
    }

    @Override
    public Result<?> getUserDetail(Long userId) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        return Result.ok(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<?> updateProfile(Long userId, SysUser profile) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        // 只允许修改部分字段
        if (StrUtil.isNotBlank(profile.getUserName())) {
            user.setUserName(profile.getUserName());
        }
        if (StrUtil.isNotBlank(profile.getPhone())) {
            user.setPhone(profile.getPhone());
        }
        if (StrUtil.isNotBlank(profile.getEmail())) {
            user.setEmail(profile.getEmail());
        }
        if (StrUtil.isNotBlank(profile.getAvatar())) {
            user.setAvatar(profile.getAvatar());
        }

        sysUserMapper.updateById(user);
        return Result.ok("个人信息修改成功");
    }
}
