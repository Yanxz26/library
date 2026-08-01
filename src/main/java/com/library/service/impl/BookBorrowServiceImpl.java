package com.library.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.common.exception.BusinessException;
import com.library.common.result.PageResult;
import com.library.common.result.Result;
import com.library.common.result.ResultCode;
import com.library.common.utils.ExcelUtil;
import com.library.dto.BorrowQueryDTO;
import com.library.entity.*;
import com.library.mapper.*;
import com.library.service.BookBorrowService;
import com.library.service.BookReserveService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * 图书借阅业务服务实现 - 核心业务模块
 *
 * @author Library Team
 */
@Slf4j
@Service
public class BookBorrowServiceImpl implements BookBorrowService {

    @Autowired
    private BookBorrowMapper bookBorrowMapper;

    @Autowired
    private BookInfoMapper bookInfoMapper;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private BookOverdueMapper bookOverdueMapper;

    @Autowired
    private BookReserveService bookReserveService;

    @Value("${library.config.default-borrow-days}")
    private Integer defaultBorrowDays;

    @Value("${library.config.max-renew-count}")
    private Integer maxRenewCount;

    @Value("${library.config.renew-days}")
    private Integer renewDays;

    @Value("${library.config.overdue-fine-per-day}")
    private BigDecimal overdueFinePerDay;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<?> borrow(Long userId, Long bookId) {
        // 1. 校验用户账号状态
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        if (user.getStatus() == 0) {
            throw new BusinessException(ResultCode.USER_DISABLED);
        }

        // 2. 校验是否存在未处理逾期
        Long overdueCount = bookOverdueMapper.selectCount(
                new LambdaQueryWrapper<BookOverdue>()
                        .eq(BookOverdue::getUserId, userId)
                        .eq(BookOverdue::getPayStatus, 0));
        if (overdueCount > 0) {
            throw new BusinessException(ResultCode.BORROW_OVERDUE_EXIST);
        }

        // 3. 校验借阅数量限制
        Long currentBorrowCount = bookBorrowMapper.selectCount(
                new LambdaQueryWrapper<BookBorrow>()
                        .eq(BookBorrow::getUserId, userId)
                        .in(BookBorrow::getBorrowStatus, 1, 3));
        if (currentBorrowCount >= user.getMaxBorrow()) {
            throw new BusinessException(ResultCode.BORROW_LIMIT_EXCEED,
                    "当前已借" + currentBorrowCount + "本，最大可借" + user.getMaxBorrow() + "本");
        }

        // 4. 校验图书状态和库存
        BookInfo book = bookInfoMapper.selectById(bookId);
        if (book == null) {
            throw new BusinessException(ResultCode.BOOK_NOT_FOUND);
        }
        if (book.getStatus() == 0) {
            throw new BusinessException(ResultCode.BOOK_OFF_SHELF);
        }
        if (book.getRemainNum() <= 0) {
            throw new BusinessException(ResultCode.BOOK_NOT_AVAILABLE);
        }

        // 5. 检查是否已有未归还的同一本书
        Long existBorrow = bookBorrowMapper.selectCount(
                new LambdaQueryWrapper<BookBorrow>()
                        .eq(BookBorrow::getUserId, userId)
                        .eq(BookBorrow::getBookId, bookId)
                        .eq(BookBorrow::getBorrowStatus, 1));
        if (existBorrow > 0) {
            throw new BusinessException(ResultCode.BOOK_BORROW_EXIST);
        }

        // 6. 生成借阅订单
        BookBorrow borrow = new BookBorrow();
        borrow.setUserId(userId);
        borrow.setBookId(bookId);
        borrow.setBorrowTime(LocalDateTime.now());
        borrow.setExpireTime(LocalDateTime.now().plusDays(defaultBorrowDays));
        borrow.setRenewCount(0);
        borrow.setBorrowStatus(1);
        bookBorrowMapper.insert(borrow);

        // 7. 扣除库存
        book.setRemainNum(book.getRemainNum() - 1);
        bookInfoMapper.updateById(book);

        // 8. 更新用户已借数量
        user.setBorrowNum(user.getBorrowNum() + 1);
        sysUserMapper.updateById(user);

        log.info("图书借阅成功: userId={}, bookId={}, borrowId={}, expireTime={}",
                userId, bookId, borrow.getId(), borrow.getExpireTime());

        return Result.ok("借阅成功，请在" + borrow.getExpireTime().toLocalDate() + "前归还");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<?> returnBook(Long userId, Long borrowId) {
        // 1. 查询借阅订单
        BookBorrow borrow = bookBorrowMapper.selectById(borrowId);
        if (borrow == null) {
            throw new BusinessException(ResultCode.BORROW_NOT_FOUND);
        }
        if (borrow.getBorrowStatus() == 2) {
            throw new BusinessException(ResultCode.BORROW_ALREADY_RETURNED);
        }

        // 权限校验：只能归还自己的借阅记录
        if (!borrow.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "只能归还自己的借阅记录");
        }

        return processReturn(borrow);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<?> adminReturn(Long adminId, Long borrowId) {
        BookBorrow borrow = bookBorrowMapper.selectById(borrowId);
        if (borrow == null) {
            throw new BusinessException(ResultCode.BORROW_NOT_FOUND);
        }
        if (borrow.getBorrowStatus() == 2) {
            throw new BusinessException(ResultCode.BORROW_ALREADY_RETURNED);
        }

        log.info("管理员线下登记归还: adminId={}, borrowId={}", adminId, borrowId);
        return processReturn(borrow);
    }

    /**
     * 归还核心处理逻辑
     */
    private Result<?> processReturn(BookBorrow borrow) {
        LocalDateTime now = LocalDateTime.now();
        boolean isOverdue = now.isAfter(borrow.getExpireTime());

        // 更新借阅订单
        borrow.setReturnTime(now);
        borrow.setBorrowStatus(isOverdue ? 3 : 2);
        bookBorrowMapper.updateById(borrow);

        // 恢复图书库存
        BookInfo book = bookInfoMapper.selectById(borrow.getBookId());
        if (book != null) {
            book.setRemainNum(book.getRemainNum() + 1);
            bookInfoMapper.updateById(book);
        }

        // 更新用户已借数量
        SysUser user = sysUserMapper.selectById(borrow.getUserId());
        if (user != null && user.getBorrowNum() > 0) {
            user.setBorrowNum(user.getBorrowNum() - 1);
            sysUserMapper.updateById(user);
        }

        // 处理逾期
        if (isOverdue) {
            long overdueDays = ChronoUnit.DAYS.between(borrow.getExpireTime(), now);
            overdueDays = Math.max(overdueDays, 1); // 至少1天

            BookOverdue overdue = new BookOverdue();
            overdue.setBorrowId(borrow.getId());
            overdue.setUserId(borrow.getUserId());
            overdue.setBookId(borrow.getBookId());
            overdue.setOverdueDays((int) overdueDays);
            overdue.setFineMoney(overdueFinePerDay.multiply(new BigDecimal(overdueDays)));
            overdue.setPayStatus(0);
            bookOverdueMapper.insert(overdue);

            log.warn("图书逾期归还: borrowId={}, overdueDays={}, fine={}",
                    borrow.getId(), overdueDays, overdue.getFineMoney());

            return Result.ok("图书归还成功，已逾期" + overdueDays + "天，罚款" + overdue.getFineMoney() + "元");
        }

        // 处理预约匹配
        bookReserveService.matchReserve(borrow.getBookId());

        return Result.ok("图书归还成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<?> renew(Long userId, Long borrowId) {
        // 1. 查询借阅订单
        BookBorrow borrow = bookBorrowMapper.selectById(borrowId);
        if (borrow == null) {
            throw new BusinessException(ResultCode.BORROW_NOT_FOUND);
        }
        if (!borrow.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "只能续借自己的借阅记录");
        }

        // 2. 校验逾期
        if (LocalDateTime.now().isAfter(borrow.getExpireTime())) {
            throw new BusinessException(ResultCode.BORROW_EXPIRED_NO_RENEW);
        }

        // 3. 校验续借次数
        if (borrow.getRenewCount() >= maxRenewCount) {
            throw new BusinessException(ResultCode.BORROW_ALREADY_RENEWED);
        }

        // 4. 校验借阅状态
        if (borrow.getBorrowStatus() != 1) {
            throw new BusinessException(ResultCode.BORROW_NOT_FOUND, "当前状态不可续借");
        }

        // 5. 执行续借
        borrow.setRenewCount(borrow.getRenewCount() + 1);
        borrow.setExpireTime(borrow.getExpireTime().plusDays(renewDays));
        bookBorrowMapper.updateById(borrow);

        log.info("图书续借成功: borrowId={}, newExpireTime={}, renewCount={}",
                borrow.getId(), borrow.getExpireTime(), borrow.getRenewCount());

        return Result.ok("续借成功，新的到期时间: " + borrow.getExpireTime().toLocalDate());
    }

    @Override
    public PageResult<BookBorrow> pageQuery(BorrowQueryDTO queryDTO) {
        Page<BookBorrow> page = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
        LambdaQueryWrapper<BookBorrow> wrapper = new LambdaQueryWrapper<>();

        if (queryDTO.getUserId() != null) {
            wrapper.eq(BookBorrow::getUserId, queryDTO.getUserId());
        }
        if (queryDTO.getBookId() != null) {
            wrapper.eq(BookBorrow::getBookId, queryDTO.getBookId());
        }
        if (queryDTO.getBorrowStatus() != null) {
            wrapper.eq(BookBorrow::getBorrowStatus, queryDTO.getBorrowStatus());
        }

        wrapper.orderByDesc(BookBorrow::getCreateTime);
        Page<BookBorrow> result = bookBorrowMapper.selectPage(page, wrapper);

        return PageResult.of(result.getCurrent(), result.getSize(),
                result.getTotal(), result.getRecords());
    }

    @Override
    public PageResult<BookBorrow> getUserBorrows(Long userId, BorrowQueryDTO queryDTO) {
        queryDTO.setUserId(userId);
        PageResult<BookBorrow> pageResult = pageQuery(queryDTO);
        
        for (BookBorrow borrow : pageResult.getRecords()) {
            BookInfo book = bookInfoMapper.selectById(borrow.getBookId());
            if (book != null) {
                borrow.setBookName(book.getBookName());
                borrow.setAuthor(book.getAuthor());
                borrow.setPublisher(book.getPublisher());
                borrow.setIsbn(book.getIsbn());
            }
        }
        
        return pageResult;
    }

    @Override
    public void exportBorrows(BorrowQueryDTO queryDTO, HttpServletResponse response) {
        PageResult<BookBorrow> pageResult = pageQuery(queryDTO);
        String[] headers = {"借阅ID", "用户ID", "图书ID", "借阅时间", "到期时间", "归还时间", "续借次数", "状态"};
        String[] fields = {"id", "userId", "bookId", "borrowTime", "expireTime",
                "returnTime", "renewCount", "borrowStatus"};
        ExcelUtil.export(response, "借阅记录", pageResult.getRecords(), headers, fields);
    }
}
