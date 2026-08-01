package com.library.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 统计数据查询DTO
 *
 * @author Library Team
 */
@Data
@Schema(description = "统计数据查询参数")
public class StatisticsDTO {

    @Schema(description = "开始日期")
    private String startDate;

    @Schema(description = "结束日期")
    private String endDate;

    @Schema(description = "统计类型: day-日, month-月, year-年")
    private String statType;
}
