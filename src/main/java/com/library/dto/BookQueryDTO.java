package com.library.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 图书查询DTO
 *
 * @author Library Team
 */
@Data
@Schema(description = "图书查询参数")
public class BookQueryDTO {

    @Schema(description = "页码", example = "1")
    private Long current = 1L;

    @Schema(description = "每页条数", example = "10")
    private Long size = 10L;

    @Schema(description = "图书名称（模糊查询）")
    private String bookName;

    @Schema(description = "作者（模糊查询）")
    private String author;

    @Schema(description = "ISBN编号")
    private String isbn;

    @Schema(description = "图书编号")
    private String bookNo;

    @Schema(description = "分类ID")
    private Long categoryId;

    @Schema(description = "出版社")
    private String publisher;

    @Schema(description = "状态（0下架，1正常）")
    private Integer status;

    @Schema(description = "排序类型: hot-热门, new-新书上架, available-可借")
    private String sortType;
}
