package com.library.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户表 sys_user
 *
 * @author Library Team
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
public class SysUser extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 登录账号（学号/工号） */
    private String userAccount;

    /** 用户姓名 */
    private String userName;

    /** 加密密码（MD5） */
    private String password;

    /** 关联角色ID */
    private Long roleId;

    /** 用户类型（1学生，2教师，3管理员） */
    private Integer userType;

    /** 联系电话 */
    private String phone;

    /** 邮箱 */
    private String email;

    /** 头像地址 */
    private String avatar;

    /** 当前已借图书数量 */
    private Integer borrowNum;

    /** 最大可借数量 */
    private Integer maxBorrow;

    /** 账号状态（0禁用，1正常） */
    private Integer status;
}
