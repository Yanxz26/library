package com.library.service;

import com.library.common.result.PageResult;
import com.library.entity.SysLog;

import javax.servlet.http.HttpServletResponse;

/**
 * 系统日志服务接口
 *
 * @author Library Team
 */
public interface SysLogService {

    /**
     * 分页查询日志
     */
    PageResult<SysLog> pageQuery(Integer logType, Long current, Long size);

    /**
     * 记录日志
     */
    void saveLog(Long userId, Integer logType, String operation,
                 String requestUrl, String requestMethod, String ipAddr, String errorMsg);

    /**
     * 清除过期日志
     */
    void cleanExpiredLogs();

    /**
     * 导出日志
     */
    void exportLogs(Integer logType, HttpServletResponse response);
}
