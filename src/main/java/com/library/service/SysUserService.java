package com.library.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.library.common.result.PageResult;
import com.library.common.result.Result;
import com.library.dto.UserQueryDTO;
import com.library.dto.UserSaveDTO;
import com.library.entity.SysUser;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;

/**
 * 用户管理服务接口
 *
 * @author Library Team
 */
public interface SysUserService extends IService<SysUser> {

    /**
     * 分页查询用户列表
     */
    PageResult<SysUser> pageQuery(UserQueryDTO queryDTO);

    /**
     * 新增用户
     */
    Result<?> addUser(UserSaveDTO dto);

    /**
     * 修改用户
     */
    Result<?> updateUser(UserSaveDTO dto);

    /**
     * 删除用户（逻辑删除）
     */
    Result<?> deleteUser(Long userId);

    /**
     * 修改用户状态（启用/禁用）
     */
    Result<?> updateStatus(Long userId, Integer status);

    /**
     * 批量导入用户
     */
    Result<?> batchImport(MultipartFile file);

    /**
     * 导出用户数据
     */
    void exportUsers(UserQueryDTO queryDTO, HttpServletResponse response);

    /**
     * 获取用户详情
     */
    Result<?> getUserDetail(Long userId);

    /**
     * 修改个人信息
     */
    Result<?> updateProfile(Long userId, SysUser user);
}
