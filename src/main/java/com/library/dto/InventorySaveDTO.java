package com.library.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 盘点提交DTO
 *
 * @author Library Team
 */
@Data
@Schema(description = "盘点提交参数")
public class InventorySaveDTO {

    @NotNull(message = "图书ID不能为空")
    @Schema(description = "图书ID", required = true)
    private Long bookId;

    @NotNull(message = "实际库存数量不能为空")
    @Schema(description = "实际盘点数量", required = true)
    private Integer actualNum;

    @Schema(description = "盘点备注")
    private String remark;
}
