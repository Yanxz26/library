package com.library.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.common.exception.BusinessException;
import com.library.common.result.PageResult;
import com.library.common.result.Result;
import com.library.common.result.ResultCode;
import com.library.common.utils.ExcelUtil;
import com.library.entity.BookInfo;
import com.library.entity.BookOverdue;
import com.library.entity.SysUser;
import com.library.mapper.BookInfoMapper;
import com.library.mapper.BookOverdueMapper;
import com.library.mapper.SysUserMapper;
import com.library.service.BookOverdueService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 逾期管理服务实现
 *
 * @author Library Team
 */
@Slf4j
@Service
public class BookOverdueServiceImpl implements BookOverdueService {

    @Autowired
    private BookOverdueMapper bookOverdueMapper;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private BookInfoMapper bookInfoMapper;

    @Override
    public PageResult<BookOverdue> pageQuery(Long userId, Integer payStatus, Long current, Long size) {
        return pageQuery(userId, null, payStatus, current, size);
    }

    @Override
    public PageResult<BookOverdue> pageQuery(Long userId, String bookName, Integer payStatus, Long current, Long size) {
        Page<BookOverdue> page = new Page<>(current, size);
        LambdaQueryWrapper<BookOverdue> wrapper = new LambdaQueryWrapper<>();

        if (userId != null) {
            wrapper.eq(BookOverdue::getUserId, userId);
        }
        if (StringUtils.hasText(bookName)) {
            wrapper.inSql(BookOverdue::getBookId,
                    "SELECT id FROM book_info WHERE book_name LIKE CONCAT('%', #{bookName}, '%')");
        }
        if (payStatus != null) {
            wrapper.eq(BookOverdue::getPayStatus, payStatus);
        }
        wrapper.orderByDesc(BookOverdue::getCreateTime);

        Page<BookOverdue> result = bookOverdueMapper.selectPage(page, wrapper);
        
        for (BookOverdue overdue : result.getRecords()) {
            SysUser user = sysUserMapper.selectById(overdue.getUserId());
            if (user != null) {
                overdue.setUserName(user.getUserName());
            }
            BookInfo book = bookInfoMapper.selectById(overdue.getBookId());
            if (book != null) {
                overdue.setBookName(book.getBookName());
                overdue.setAuthor(book.getAuthor());
            }
        }
        
        return PageResult.of(result.getCurrent(), result.getSize(), result.getTotal(), result.getRecords());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<?> payFine(Long userId, Long overdueId) {
        BookOverdue overdue = bookOverdueMapper.selectById(overdueId);
        if (overdue == null) {
            throw new BusinessException(ResultCode.OVERDUE_NOT_FOUND);
        }
        
        SysUser user = sysUserMapper.selectById(userId);
        boolean isAdmin = user != null && ("admin".equals(user.getUserType()) || "library".equals(user.getUserType()));
        
        if (!isAdmin && !overdue.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "只能缴纳自己的罚款");
        }
        
        if (overdue.getPayStatus() == 1) {
            return Result.fail("罚款已缴纳，无需重复操作");
        }

        overdue.setPayStatus(1);
        overdue.setPayTime(LocalDateTime.now());
        bookOverdueMapper.updateById(overdue);

        log.info("用户缴纳逾期罚款: userId={}, overdueId={}, amount={}",
                userId, overdueId, overdue.getFineMoney());
        return Result.ok("罚款缴纳成功，金额: " + overdue.getFineMoney() + "元");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<?> waiveFine(Long adminId, Long overdueId, String reason) {
        BookOverdue overdue = bookOverdueMapper.selectById(overdueId);
        if (overdue == null) {
            throw new BusinessException(ResultCode.OVERDUE_NOT_FOUND);
        }

        overdue.setFineMoney(BigDecimal.ZERO);
        overdue.setPayStatus(1);
        overdue.setPayTime(LocalDateTime.now());
        bookOverdueMapper.updateById(overdue);

        log.warn("管理员减免逾期罚款: adminId={}, overdueId={}, reason={}", adminId, overdueId, reason);
        return Result.ok("罚款已减免");
    }

    @Override
    public Result<?> getUserOverdueSummary(Long userId) {
        // 查询未缴费逾期记录
        Long unpaidCount = bookOverdueMapper.selectCount(
                new LambdaQueryWrapper<BookOverdue>()
                        .eq(BookOverdue::getUserId, userId)
                        .eq(BookOverdue::getPayStatus, 0));

        // 查询总罚款金额
        BigDecimal totalFine = BigDecimal.ZERO;
        if (unpaidCount > 0) {
            // 计算未缴费罚款总额
            totalFine = bookOverdueMapper.selectList(
                    new LambdaQueryWrapper<BookOverdue>()
                            .eq(BookOverdue::getUserId, userId)
                            .eq(BookOverdue::getPayStatus, 0))
                    .stream()
                    .map(BookOverdue::getFineMoney)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        // 检查是否限制借阅
        boolean canBorrow = unpaidCount == 0;
        SysUser user = sysUserMapper.selectById(userId);
        if (user != null && user.getStatus() == 0) {
            canBorrow = false;
        }

        return Result.ok(new OverdueSummary(unpaidCount, totalFine, canBorrow));
    }

    /**
     * 内部类：逾期汇总
     */
    @lombok.Data
    @lombok.AllArgsConstructor
    private static class OverdueSummary {
        private Long unpaidCount;
        private BigDecimal totalFine;
        private boolean canBorrow;
    }

    @Override
    public void exportOverdue(HttpServletResponse response) {
        PageResult<BookOverdue> pageResult = pageQuery(null, null, null, 1L, 999999L);
        String[] headers = {"逾期ID", "借阅订单ID", "用户ID", "图书ID", "逾期天数", "罚款金额", "缴费状态", "缴费时间"};
        String[] fields = {"id", "borrowId", "userId", "bookId", "overdueDays", "fineMoney", "payStatus", "payTime"};
        ExcelUtil.export(response, "逾期记录", pageResult.getRecords(), headers, fields);
    }
}
