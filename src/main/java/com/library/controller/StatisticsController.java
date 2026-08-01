package com.library.controller;

import com.library.common.annotation.Log;
import com.library.common.result.Result;
import com.library.dto.StatisticsDTO;
import com.library.service.StatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

/**
 * 数据统计控制器
 *
 * @author Library Team
 */
@Tag(name = "数据统计", description = "借阅统计、用户统计、图书统计、报表导出接口")
@RestController
@RequestMapping("/sys/statistics")
public class StatisticsController {

    @Autowired
    private StatisticsService statisticsService;

    @Operation(summary = "借阅数据统计")
    @GetMapping("/borrow")
    public Result<?> getBorrowStatistics(StatisticsDTO dto) {
        return statisticsService.getBorrowStatistics(
                dto.getStatType(), dto.getStartDate(), dto.getEndDate());
    }

    @Operation(summary = "热门借阅图书TOP10")
    @GetMapping("/hot-books")
    public Result<?> getHotBooksTop10() {
        return statisticsService.getHotBooksTop10();
    }

    @Operation(summary = "用户数据统计")
    @GetMapping("/user")
    public Result<?> getUserStatistics() {
        return statisticsService.getUserStatistics();
    }

    @Operation(summary = "图书数据统计")
    @GetMapping("/book")
    public Result<?> getBookStatistics() {
        return statisticsService.getBookStatistics();
    }

    @Operation(summary = "当前用户个人统计")
    @GetMapping("/my-stats")
    public Result<?> getMyStatistics() {
        return statisticsService.getMyStatistics();
    }

    @Operation(summary = "导出借阅统计报表")
    @Log(type = 1, value = "导出借阅统计报表")
    @PreAuthorize("hasAnyRole('admin', 'library')")
    @GetMapping("/export-borrow")
    public void exportBorrowReport(@RequestParam(required = false) String startDate,
                                    @RequestParam(required = false) String endDate,
                                    HttpServletResponse response) {
        statisticsService.exportBorrowReport(startDate, endDate, response);
    }

    @Operation(summary = "导出逾期记录报表")
    @Log(type = 1, value = "导出逾期记录报表")
    @PreAuthorize("hasAnyRole('admin', 'library')")
    @GetMapping("/export-overdue")
    public void exportOverdueReport(HttpServletResponse response) {
        statisticsService.exportOverdueReport(response);
    }
}
