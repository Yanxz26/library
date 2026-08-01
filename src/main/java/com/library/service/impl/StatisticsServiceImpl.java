package com.library.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.library.common.result.Result;
import com.library.common.utils.ExcelUtil;
import com.library.entity.*;
import com.library.mapper.*;
import com.library.service.StatisticsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import com.library.security.SecurityUser;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 数据统计服务实现
 *
 * @author Library Team
 */
@Slf4j
@Service
public class StatisticsServiceImpl implements StatisticsService {

    @Autowired
    private BookBorrowMapper bookBorrowMapper;

    @Autowired
    private BookOverdueMapper bookOverdueMapper;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private BookInfoMapper bookInfoMapper;

    @Autowired
    private BookCategoryMapper bookCategoryMapper;

    @Autowired
    private BookLossMapper bookLossMapper;

    @Autowired
    private BookReserveMapper bookReserveMapper;

    @Override
    public Result<?> getBorrowStatistics(String statType, String startDate, String endDate) {
        if ("day".equals(statType) && startDate != null && endDate != null) {
            return getBorrowTrendByDay(startDate, endDate);
        }

        Map<String, Object> statistics = new HashMap<>();

        // 总借阅量
        Long totalBorrow = bookBorrowMapper.selectCount(null);
        statistics.put("totalBorrow", totalBorrow);

        // 总归还量
        Long totalReturn = bookBorrowMapper.selectCount(
                new LambdaQueryWrapper<BookBorrow>()
                        .eq(BookBorrow::getBorrowStatus, 2));
        statistics.put("totalReturn", totalReturn);

        // 当前借阅中
        Long currentBorrowing = bookBorrowMapper.selectCount(
                new LambdaQueryWrapper<BookBorrow>()
                        .eq(BookBorrow::getBorrowStatus, 1));
        statistics.put("currentBorrowing", currentBorrowing);
        statistics.put("borrowingCount", currentBorrowing);

        // 逾期中
        Long currentOverdue = bookBorrowMapper.selectCount(
                new LambdaQueryWrapper<BookBorrow>()
                        .eq(BookBorrow::getBorrowStatus, 3));
        statistics.put("currentOverdue", currentOverdue);
        statistics.put("overdueCount", currentOverdue);

        // 逾期率
        if (totalBorrow > 0) {
            Long totalOverdue = bookOverdueMapper.selectCount(null);
            double overdueRate = (double) totalOverdue / totalBorrow * 100;
            statistics.put("overdueRate", String.format("%.2f%%", overdueRate));
            statistics.put("totalOverdue", totalOverdue);
        } else {
            statistics.put("overdueRate", "0.00%");
            statistics.put("totalOverdue", 0L);
        }

        // 续借总量
        Long totalRenew = bookBorrowMapper.selectCount(
                new LambdaQueryWrapper<BookBorrow>()
                        .gt(BookBorrow::getRenewCount, 0));
        statistics.put("totalRenew", totalRenew);

        return Result.ok(statistics);
    }

    private Result<?> getBorrowTrendByDay(String startDate, String endDate) {
        List<Map<String, Object>> trendData = new ArrayList<>();
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate start = LocalDate.parse(startDate, formatter);
        LocalDate end = LocalDate.parse(endDate, formatter);
        
        while (!start.isAfter(end)) {
            String dateStr = start.format(formatter);
            Map<String, Object> dayData = new HashMap<>();
            dayData.put("date", dateStr);
            dayData.put("name", dateStr.substring(5));
            
            Long count = bookBorrowMapper.selectCount(
                    new LambdaQueryWrapper<BookBorrow>()
                            .ge(BookBorrow::getBorrowTime, dateStr + " 00:00:00")
                            .lt(BookBorrow::getBorrowTime, dateStr + " 23:59:59"));
            dayData.put("count", count);
            dayData.put("value", count);
            
            trendData.add(dayData);
            start = start.plusDays(1);
        }
        
        return Result.ok(trendData);
    }

    @Override
    public Result<?> getHotBooksTop10() {
        // 按借阅次数排序（简化处理：按总数量降序）
        List<BookInfo> books = bookInfoMapper.selectList(
                new LambdaQueryWrapper<BookInfo>()
                        .eq(BookInfo::getStatus, 1)
                        .orderByDesc(BookInfo::getTotalNum)
                        .last("LIMIT 10"));

        List<Map<String, Object>> result = new ArrayList<>();
        for (BookInfo book : books) {
            Long borrowCount = bookBorrowMapper.selectCount(
                    new LambdaQueryWrapper<BookBorrow>()
                            .eq(BookBorrow::getBookId, book.getId()));
            Map<String, Object> item = new HashMap<>();
            item.put("bookId", book.getId());
            item.put("bookName", book.getBookName());
            item.put("author", book.getAuthor());
            item.put("borrowCount", borrowCount);
            result.add(item);
        }

        // 按借阅次数排序
        result.sort((a, b) -> Long.compare(
                (Long) b.get("borrowCount"), (Long) a.get("borrowCount")));

        return Result.ok(result);
    }

    @Override
    public Result<?> getUserStatistics() {
        Map<String, Object> statistics = new HashMap<>();

        // 总用户数
        Long totalUsers = sysUserMapper.selectCount(null);
        statistics.put("totalUsers", totalUsers);

        // 学生数量
        Long studentCount = sysUserMapper.selectCount(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getUserType, 1));
        statistics.put("studentCount", studentCount);

        // 教师数量
        Long teacherCount = sysUserMapper.selectCount(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getUserType, 2));
        statistics.put("teacherCount", teacherCount);

        // 管理员数量
        Long adminCount = sysUserMapper.selectCount(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getUserType, 3));
        statistics.put("adminCount", adminCount);

        // 活跃用户数（有借阅记录的用户）
        List<BookBorrow> borrows = bookBorrowMapper.selectList(null);
        long activeUsers = borrows.stream()
                .map(BookBorrow::getUserId)
                .distinct()
                .count();
        statistics.put("activeUsers", activeUsers);

        // 师生借阅占比
        if (totalUsers > 0) {
            double studentRatio = (double) studentCount / totalUsers * 100;
            double teacherRatio = (double) teacherCount / totalUsers * 100;
            statistics.put("studentRatio", String.format("%.1f%%", studentRatio));
            statistics.put("teacherRatio", String.format("%.1f%%", teacherRatio));
        }

        return Result.ok(statistics);
    }

    @Override
    public Result<?> getBookStatistics() {
        Map<String, Object> statistics = new HashMap<>();

        // 图书总数
        Long totalBooks = bookInfoMapper.selectCount(null);
        statistics.put("totalBooks", totalBooks);

        // 上架图书数
        Long onShelfBooks = bookInfoMapper.selectCount(
                new LambdaQueryWrapper<BookInfo>()
                        .eq(BookInfo::getStatus, 1));
        statistics.put("onShelfBooks", onShelfBooks);

        // 下架图书数
        Long offShelfBooks = totalBooks - onShelfBooks;
        statistics.put("offShelfBooks", offShelfBooks);

        // 库存空缺图书（剩余库存为0）
        Long emptyStockBooks = bookInfoMapper.selectCount(
                new LambdaQueryWrapper<BookInfo>()
                        .eq(BookInfo::getRemainNum, 0)
                        .eq(BookInfo::getStatus, 1));
        statistics.put("emptyStockBooks", emptyStockBooks);

        // 总库存量
        List<BookInfo> allBooks = bookInfoMapper.selectList(null);
        long totalStock = allBooks.stream().mapToInt(BookInfo::getTotalNum).sum();
        long remainStock = allBooks.stream().mapToInt(BookInfo::getRemainNum).sum();
        statistics.put("totalStock", totalStock);
        statistics.put("remainStock", remainStock);

        // 损耗总量
        List<BookLoss> allLoss = bookLossMapper.selectList(null);
        long totalLoss = allLoss.stream().mapToInt(BookLoss::getLossNum).sum();
        statistics.put("totalLoss", totalLoss);

        // 各分类图书数量
        List<BookCategory> categories = bookCategoryMapper.selectList(null);
        List<Map<String, Object>> categoryStats = new ArrayList<>();
        for (BookCategory category : categories) {
            Long count = bookInfoMapper.selectCount(
                    new LambdaQueryWrapper<BookInfo>()
                            .eq(BookInfo::getCategoryId, category.getId()));
            Map<String, Object> item = new HashMap<>();
            item.put("categoryName", category.getCategoryName());
            item.put("count", count);
            categoryStats.add(item);
        }
        statistics.put("categoryStats", categoryStats);

        return Result.ok(statistics);
    }

    @Override
    public void exportBorrowReport(String startDate, String endDate, HttpServletResponse response) {
        List<BookBorrow> borrowList = bookBorrowMapper.selectList(null);
        String[] headers = {"借阅ID", "用户ID", "图书ID", "借阅时间", "到期时间", "归还时间", "续借次数", "状态"};
        String[] fields = {"id", "userId", "bookId", "borrowTime", "expireTime",
                "returnTime", "renewCount", "borrowStatus"};
        ExcelUtil.export(response, "借阅统计报表", borrowList, headers, fields);
    }

    @Override
    public void exportOverdueReport(HttpServletResponse response) {
        List<BookOverdue> overdueList = bookOverdueMapper.selectList(null);
        String[] headers = {"逾期ID", "借阅订单ID", "用户ID", "图书ID", "逾期天数", "罚款金额", "缴费状态", "缴费时间"};
        String[] fields = {"id", "borrowId", "userId", "bookId", "overdueDays",
                "fineMoney", "payStatus", "payTime"};
        ExcelUtil.export(response, "逾期记录报表", overdueList, headers, fields);
    }

    @Override
    public Result<?> getMyStatistics() {
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            return Result.fail("未登录");
        }

        Map<String, Object> statistics = new HashMap<>();

        // 当前借阅中数量
        Long borrowingCount = bookBorrowMapper.selectCount(
                new LambdaQueryWrapper<BookBorrow>()
                        .eq(BookBorrow::getUserId, currentUserId)
                        .eq(BookBorrow::getBorrowStatus, 1));
        statistics.put("borrowingCount", borrowingCount);
        statistics.put("currentBorrowing", borrowingCount);

        // 逾期数量
        Long overdueCount = bookBorrowMapper.selectCount(
                new LambdaQueryWrapper<BookBorrow>()
                        .eq(BookBorrow::getUserId, currentUserId)
                        .eq(BookBorrow::getBorrowStatus, 3));
        statistics.put("overdueCount", overdueCount);
        statistics.put("currentOverdue", overdueCount);

        // 即将到期数量（7天内）
        LocalDate today = LocalDate.now();
        LocalDate sevenDaysLater = today.plusDays(7);
        Long expiringCount = bookBorrowMapper.selectCount(
                new LambdaQueryWrapper<BookBorrow>()
                        .eq(BookBorrow::getUserId, currentUserId)
                        .eq(BookBorrow::getBorrowStatus, 1)
                        .le(BookBorrow::getExpireTime, sevenDaysLater));
        statistics.put("expiringCount", expiringCount);

        // 当前预约数量
        Long reserveCount = bookReserveMapper.selectCount(
                new LambdaQueryWrapper<BookReserve>()
                        .eq(BookReserve::getUserId, currentUserId)
                        .eq(BookReserve::getReserveStatus, 1));
        statistics.put("reserveCount", reserveCount);

        return Result.ok(statistics);
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof SecurityUser) {
            SecurityUser securityUser = (SecurityUser) authentication.getPrincipal();
            return securityUser.getSysUser().getId();
        }
        return null;
    }
}
