package com.library.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 损耗登记DTO
 *
 * @author Library Team
 */
@Data
@Schema(description = "损耗登记参数")
public class LossSaveDTO {

    @NotNull(message = "图书ID不能为空")
    @Schema(description = "图书ID", required = true)
    private Long bookId;

    @NotNull(message = "损耗数量不能为空")
    @Schema(description = "损耗数量", required = true)
    private Integer lossNum;

    @NotNull(message = "损耗类型不能为空")
    @Schema(description = "损耗类型（1破损，2丢失，3报废）", required = true)
    private Integer lossType;

    @Schema(description = "损耗原因")
    private String lossReason;
}
