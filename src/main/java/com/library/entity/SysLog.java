package com.library.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 系统日志表 sys_log
 *
 * @author Library Team
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_log")
public class SysLog extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 操作人ID（系统异常为空） */
    private Long userId;

    /** 日志类型（1操作日志，2业务日志，3异常日志） */
    private Integer logType;

    /** 操作描述 */
    private String operation;

    /** 请求接口地址 */
    private String requestUrl;

    /** 请求方式 */
    private String requestMethod;

    /** 操作IP地址 */
    private String ipAddr;

    /** 异常信息（异常日志专用） */
    private String errorMsg;
}
