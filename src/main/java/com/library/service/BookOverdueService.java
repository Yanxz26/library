package com.library.service;

import com.library.common.result.PageResult;
import com.library.common.result.Result;
import com.library.entity.BookOverdue;

import javax.servlet.http.HttpServletResponse;

/**
 * 逾期管理服务接口
 *
 * @author Library Team
 */
public interface BookOverdueService {

    /**
     * 分页查询逾期记录
     */
    PageResult<BookOverdue> pageQuery(Long userId, Integer payStatus, Long current, Long size);

    /**
     * 分页查询逾期记录（管理员）
     */
    PageResult<BookOverdue> pageQuery(Long userId, String bookName, Integer payStatus, Long current, Long size);

    /**
     * 手动缴纳罚款
     */
    Result<?> payFine(Long userId, Long overdueId);

    /**
     * 管理员手动减免罚款
     */
    Result<?> waiveFine(Long adminId, Long overdueId, String reason);

    /**
     * 获取用户逾期汇总
     */
    Result<?> getUserOverdueSummary(Long userId);

    /**
     * 导出逾期记录
     */
    void exportOverdue(HttpServletResponse response);
}
