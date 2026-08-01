package com.library.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 借阅查询DTO
 *
 * @author Library Team
 */
@Data
@Schema(description = "借阅记录查询参数")
public class BorrowQueryDTO {

    @Schema(description = "页码", example = "1")
    private Long current = 1L;

    @Schema(description = "每页条数", example = "10")
    private Long size = 10L;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "图书ID")
    private Long bookId;

    @Schema(description = "借阅状态（1借阅中，2已归还，3已逾期）")
    private Integer borrowStatus;

    @Schema(description = "图书名称（模糊查询）")
    private String bookName;

    @Schema(description = "用户账号")
    private String userAccount;
}
