package com.library.service;

import com.library.common.result.Result;

import javax.servlet.http.HttpServletResponse;

/**
 * 数据统计服务接口
 *
 * @author Library Team
 */
public interface StatisticsService {

    /**
     * 借阅数据统计（日/月/年借阅量、归还量、逾期率）
     */
    Result<?> getBorrowStatistics(String statType, String startDate, String endDate);

    /**
     * 热门借阅图书TOP10
     */
    Result<?> getHotBooksTop10();

    /**
     * 用户数据统计（活跃用户数、新增用户数、师生借阅占比）
     */
    Result<?> getUserStatistics();

    /**
     * 图书数据统计（各分类数量、库存空缺、损耗数量）
     */
    Result<?> getBookStatistics();

    /**
     * 导出借阅统计报表
     */
    void exportBorrowReport(String startDate, String endDate, HttpServletResponse response);

    /**
     * 导出逾期记录报表
     */
    void exportOverdueReport(HttpServletResponse response);

    /**
     * 获取当前用户个人统计数据（借阅中数量、即将到期数量、逾期数量、预约数量）
     */
    Result<?> getMyStatistics();
}
