package com.library.service;

import com.library.common.result.PageResult;
import com.library.common.result.Result;
import com.library.entity.BookReserve;

/**
 * 图书预约服务接口
 *
 * @author Library Team
 */
public interface BookReserveService {

    /**
     * 提交预约申请
     */
    Result<?> reserve(Long userId, Long bookId);

    /**
     * 取消预约
     */
    Result<?> cancelReserve(Long userId, Long reserveId);

    /**
     * 分页查询预约记录
     */
    PageResult<BookReserve> pageQuery(Long userId, Long current, Long size);

    /**
     * 分页查询预约记录（管理员）
     */
    PageResult<BookReserve> pageQuery(Long userId, String bookName, Integer reserveStatus, Long current, Long size);

    /**
     * 标记预约完成（管理员）
     */
    Result<?> completeReserve(Long reserveId);

    /**
     * 处理预约自动匹配（图书归还后触发）
     */
    void matchReserve(Long bookId);
}
