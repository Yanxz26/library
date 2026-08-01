package com.library.service;

import com.library.common.result.PageResult;
import com.library.common.result.Result;
import com.library.dto.BorrowQueryDTO;
import com.library.entity.BookBorrow;

import javax.servlet.http.HttpServletResponse;

/**
 * 图书借阅业务服务接口
 *
 * @author Library Team
 */
public interface BookBorrowService {

    /**
     * 图书借阅
     */
    Result<?> borrow(Long userId, Long bookId);

    /**
     * 图书归还
     */
    Result<?> returnBook(Long userId, Long borrowId);

    /**
     * 图书续借
     */
    Result<?> renew(Long userId, Long borrowId);

    /**
     * 管理员线下登记归还
     */
    Result<?> adminReturn(Long adminId, Long borrowId);

    /**
     * 分页查询借阅记录
     */
    PageResult<BookBorrow> pageQuery(BorrowQueryDTO queryDTO);

    /**
     * 获取用户借阅记录
     */
    PageResult<BookBorrow> getUserBorrows(Long userId, BorrowQueryDTO queryDTO);

    /**
     * 导出借阅记录
     */
    void exportBorrows(BorrowQueryDTO queryDTO, HttpServletResponse response);
}
