package com.library.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.common.result.PageResult;
import com.library.common.utils.ExcelUtil;
import com.library.entity.SysLog;
import com.library.mapper.SysLogMapper;
import com.library.service.SysLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 系统日志服务实现
 *
 * @author Library Team
 */
@Slf4j
@Service
public class SysLogServiceImpl implements SysLogService {

    @Autowired
    private SysLogMapper sysLogMapper;

    @Override
    public PageResult<SysLog> pageQuery(Integer logType, Long current, Long size) {
        Page<SysLog> page = new Page<>(current, size);
        LambdaQueryWrapper<SysLog> wrapper = new LambdaQueryWrapper<>();
        if (logType != null) {
            wrapper.eq(SysLog::getLogType, logType);
        }
        wrapper.orderByDesc(SysLog::getCreateTime);
        Page<SysLog> result = sysLogMapper.selectPage(page, wrapper);
        return PageResult.of(result.getCurrent(), result.getSize(), result.getTotal(), result.getRecords());
    }

    @Override
    @Async
    public void saveLog(Long userId, Integer logType, String operation,
                        String requestUrl, String requestMethod, String ipAddr, String errorMsg) {
        try {
            SysLog sysLog = new SysLog();
            sysLog.setUserId(userId);
            sysLog.setLogType(logType);
            sysLog.setOperation(operation);
            sysLog.setRequestUrl(requestUrl);
            sysLog.setRequestMethod(requestMethod);
            sysLog.setIpAddr(ipAddr);
            sysLog.setErrorMsg(errorMsg);
            sysLogMapper.insert(sysLog);
        } catch (Exception e) {
            log.error("记录日志失败: {}", e.getMessage());
        }
    }

    @Override
    public void cleanExpiredLogs() {
        // 清除180天前的日志
        LocalDateTime expireTime = LocalDateTime.now().minusDays(180);
        sysLogMapper.delete(
                new LambdaQueryWrapper<SysLog>()
                        .lt(SysLog::getCreateTime, expireTime));
        log.info("已清理{}天前的过期日志", 180);
    }

    @Override
    public void exportLogs(Integer logType, HttpServletResponse response) {
        PageResult<SysLog> pageResult = pageQuery(logType, 1L, 999999L);
        String[] headers = {"ID", "用户ID", "日志类型", "操作描述", "请求URL", "请求方式", "IP", "异常信息", "时间"};
        String[] fields = {"id", "userId", "logType", "operation", "requestUrl",
                "requestMethod", "ipAddr", "errorMsg", "createTime"};
        ExcelUtil.export(response, "系统日志", pageResult.getRecords(), headers, fields);
    }
}
