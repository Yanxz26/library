package com.library.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.library.entity.BookBorrow;
import com.library.entity.BookOverdue;
import com.library.entity.BookReserve;
import com.library.mapper.BookBorrowMapper;
import com.library.mapper.BookOverdueMapper;
import com.library.mapper.BookReserveMapper;
import com.library.service.SysLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * 系统定时任务
 *
 * @author Library Team
 */
@Slf4j
@Component
public class ScheduledTask {

    @Autowired
    private BookBorrowMapper bookBorrowMapper;

    @Autowired
    private BookOverdueMapper bookOverdueMapper;

    @Autowired
    private BookReserveMapper bookReserveMapper;

    @Autowired
    private SysLogService sysLogService;

    @Value("${library.config.overdue-fine-per-day}")
    private BigDecimal overdueFinePerDay;

    /**
     * 逾期自动扫描（每天凌晨2点执行）
     * 扫描所有未归还且已到期的借阅订单，标记逾期状态并生成逾期记录
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void scanOverdueBorrows() {
        log.info("========== 定时任务: 逾期扫描开始 ==========");
        try {
            LocalDateTime now = LocalDateTime.now();

            // 查询所有借阅中且已到期的订单
            List<BookBorrow> overdueBorrows = bookBorrowMapper.selectList(
                    new LambdaQueryWrapper<BookBorrow>()
                            .eq(BookBorrow::getBorrowStatus, 1)
                            .lt(BookBorrow::getExpireTime, now));

            for (BookBorrow borrow : overdueBorrows) {
                // 更新借阅状态为逾期
                borrow.setBorrowStatus(3);
                bookBorrowMapper.updateById(borrow);

                // 计算逾期天数
                long overdueDays = ChronoUnit.DAYS.between(borrow.getExpireTime(), now);
                overdueDays = Math.max(overdueDays, 1);

                // 检查是否已存在逾期记录
                Long count = bookOverdueMapper.selectCount(
                        new LambdaQueryWrapper<BookOverdue>()
                                .eq(BookOverdue::getBorrowId, borrow.getId()));
                if (count == 0) {
                    // 创建逾期记录
                    BookOverdue overdue = new BookOverdue();
                    overdue.setBorrowId(borrow.getId());
                    overdue.setUserId(borrow.getUserId());
                    overdue.setBookId(borrow.getBookId());
                    overdue.setOverdueDays((int) overdueDays);
                    overdue.setFineMoney(overdueFinePerDay.multiply(new BigDecimal(overdueDays)));
                    overdue.setPayStatus(0);
                    bookOverdueMapper.insert(overdue);
                } else {
                    // 更新已有逾期记录
                    BookOverdue existOverdue = bookOverdueMapper.selectOne(
                            new LambdaQueryWrapper<BookOverdue>()
                                    .eq(BookOverdue::getBorrowId, borrow.getId()));
                    if (existOverdue != null && existOverdue.getPayStatus() == 0) {
                        existOverdue.setOverdueDays((int) overdueDays);
                        existOverdue.setFineMoney(overdueFinePerDay.multiply(new BigDecimal(overdueDays)));
                        bookOverdueMapper.updateById(existOverdue);
                    }
                }
            }

            log.info("逾期扫描完成，处理{}条逾期记录", overdueBorrows.size());
        } catch (Exception e) {
            log.error("逾期扫描任务异常: ", e);
            sysLogService.saveLog(null, 3, "逾期扫描任务异常",
                    "/task/scanOverdue", "SCHEDULED", "127.0.0.1", e.getMessage());
        }
        log.info("========== 定时任务: 逾期扫描结束 ==========");
    }

    /**
     * 预约过期自动失效（每天凌晨3点执行）
     * 将超过有效期的预约标记为已失效
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void expireReserves() {
        log.info("========== 定时任务: 预约过期扫描开始 ==========");
        try {
            List<BookReserve> expiredReserves = bookReserveMapper.selectList(
                    new LambdaQueryWrapper<BookReserve>()
                            .eq(BookReserve::getReserveStatus, 1)
                            .lt(BookReserve::getExpireTime, LocalDateTime.now()));

            for (BookReserve reserve : expiredReserves) {
                reserve.setReserveStatus(3);
                bookReserveMapper.updateById(reserve);
            }

            log.info("预约过期扫描完成，处理{}条过期预约", expiredReserves.size());
        } catch (Exception e) {
            log.error("预约过期扫描异常: ", e);
        }
        log.info("========== 定时任务: 预约过期扫描结束 ==========");
    }

    /**
     * 日志清理（每周日凌晨4点执行）
     */
    @Scheduled(cron = "0 0 4 * * 0")
    public void cleanLogs() {
        log.info("========== 定时任务: 日志清理开始 ==========");
        try {
            sysLogService.cleanExpiredLogs();
        } catch (Exception e) {
            log.error("日志清理异常: ", e);
        }
        log.info("========== 定时任务: 日志清理结束 ==========");
    }
}
