package com.library.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.common.exception.BusinessException;
import com.library.common.result.PageResult;
import com.library.common.result.Result;
import com.library.common.result.ResultCode;
import com.library.entity.*;
import com.library.mapper.*;
import com.library.service.BookReserveService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 图书预约服务实现
 *
 * @author Library Team
 */
@Slf4j
@Service
public class BookReserveServiceImpl implements BookReserveService {

    @Autowired
    private BookReserveMapper bookReserveMapper;

    @Autowired
    private BookInfoMapper bookInfoMapper;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Value("${library.config.reserve-expire-days}")
    private Integer reserveExpireDays;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<?> reserve(Long userId, Long bookId) {
        // 检查图书是否存在
        BookInfo book = bookInfoMapper.selectById(bookId);
        if (book == null) {
            throw new BusinessException(ResultCode.BOOK_NOT_FOUND);
        }

        // 检查是否已预约过
        Long count = bookReserveMapper.selectCount(
                new LambdaQueryWrapper<BookReserve>()
                        .eq(BookReserve::getUserId, userId)
                        .eq(BookReserve::getBookId, bookId)
                        .eq(BookReserve::getReserveStatus, 1));
        if (count > 0) {
            throw new BusinessException(ResultCode.RESERVE_ALREADY_EXIST);
        }

        // 如果有库存，提示直接借阅
        if (book.getRemainNum() > 0) {
            return Result.fail(2008, "该图书有库存，请直接借阅");
        }

        // 创建预约记录
        BookReserve reserve = new BookReserve();
        reserve.setUserId(userId);
        reserve.setBookId(bookId);
        reserve.setReserveTime(LocalDateTime.now());
        reserve.setExpireTime(LocalDateTime.now().plusDays(reserveExpireDays));
        reserve.setReserveStatus(1);
        bookReserveMapper.insert(reserve);

        log.info("图书预约成功: userId={}, bookId={}, expireTime={}", userId, bookId, reserve.getExpireTime());
        return Result.ok("预约成功，有效期至" + reserve.getExpireTime().toLocalDate());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<?> cancelReserve(Long userId, Long reserveId) {
        BookReserve reserve = bookReserveMapper.selectById(reserveId);
        if (reserve == null) {
            throw new BusinessException(ResultCode.RESERVE_NOT_FOUND);
        }
        
        SysUser user = sysUserMapper.selectById(userId);
        boolean isAdmin = user != null && ("admin".equals(user.getUserType()) || "library".equals(user.getUserType()));
        
        if (!isAdmin && !reserve.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "只能取消自己的预约");
        }
        
        reserve.setReserveStatus(3);
        bookReserveMapper.updateById(reserve);
        
        log.info("预约取消: userId={}, reserveId={}, bookId={}", userId, reserveId, reserve.getBookId());
        return Result.ok("预约已取消");
    }

    @Override
    public PageResult<BookReserve> pageQuery(Long userId, Long current, Long size) {
        return pageQuery(userId, null, null, current, size);
    }

    @Override
    public PageResult<BookReserve> pageQuery(Long userId, String bookName, Integer reserveStatus, Long current, Long size) {
        Page<BookReserve> page = new Page<>(current, size);
        LambdaQueryWrapper<BookReserve> wrapper = new LambdaQueryWrapper<>();
        if (userId != null) {
            wrapper.eq(BookReserve::getUserId, userId);
        }
        if (StringUtils.hasText(bookName)) {
            wrapper.inSql(BookReserve::getBookId,
                    "SELECT id FROM book_info WHERE book_name LIKE CONCAT('%', #{bookName}, '%')");
        }
        if (reserveStatus != null) {
            wrapper.eq(BookReserve::getReserveStatus, reserveStatus);
        }
        wrapper.orderByDesc(BookReserve::getCreateTime);
        Page<BookReserve> result = bookReserveMapper.selectPage(page, wrapper);
        
        List<BookReserve> records = result.getRecords();
        for (BookReserve reserve : records) {
            SysUser user = sysUserMapper.selectById(reserve.getUserId());
            if (user != null) {
                reserve.setUserName(user.getUserName());
            }
            BookInfo book = bookInfoMapper.selectById(reserve.getBookId());
            if (book != null) {
                reserve.setBookName(book.getBookName());
                reserve.setAuthor(book.getAuthor());
                reserve.setPublisher(book.getPublisher());
            }
        }
        
        return PageResult.of(result.getCurrent(), result.getSize(), result.getTotal(), records);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<?> completeReserve(Long reserveId) {
        BookReserve reserve = bookReserveMapper.selectById(reserveId);
        if (reserve == null) {
            throw new BusinessException(ResultCode.RESERVE_NOT_FOUND);
        }
        if (reserve.getReserveStatus() != 1) {
            throw new BusinessException(ResultCode.FORBIDDEN, "只能标记待生效的预约为完成");
        }
        reserve.setReserveStatus(2); // 已完成
        bookReserveMapper.updateById(reserve);
        
        return Result.ok("预约已标记为完成");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void matchReserve(Long bookId) {
        // 查询最早的待生效预约
        BookReserve earliestReserve = bookReserveMapper.selectOne(
                new LambdaQueryWrapper<BookReserve>()
                        .eq(BookReserve::getBookId, bookId)
                        .eq(BookReserve::getReserveStatus, 1)
                        .gt(BookReserve::getExpireTime, LocalDateTime.now())
                        .orderByAsc(BookReserve::getReserveTime)
                        .last("LIMIT 1"));

        if (earliestReserve != null) {
            // 标记预约为已完成（已通知）
            earliestReserve.setReserveStatus(2);
            bookReserveMapper.updateById(earliestReserve);
            log.info("图书归还后自动匹配预约: bookId={}, userId={}", bookId, earliestReserve.getUserId());
            // TODO: 集成消息推送通知用户
        }
    }
}
