package com.library.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 图书新增/修改DTO
 *
 * @author Library Team
 */
@Data
@Schema(description = "图书新增/修改参数")
public class BookSaveDTO {

    @Schema(description = "图书ID（修改时必填）")
    private Long id;

    @Schema(description = "图书编号（不传则自动生成）")
    private String bookNo;

    @NotBlank(message = "ISBN不能为空")
    @Schema(description = "ISBN编号", required = true)
    private String isbn;

    @NotBlank(message = "图书名称不能为空")
    @Schema(description = "图书名称", required = true)
    private String bookName;

    @NotBlank(message = "作者不能为空")
    @Schema(description = "作者", required = true)
    private String author;

    @NotBlank(message = "出版社不能为空")
    @Schema(description = "出版社", required = true)
    private String publisher;

    @Schema(description = "出版时间")
    private LocalDate publishTime;

    @NotNull(message = "分类不能为空")
    @Schema(description = "分类ID", required = true)
    private Long categoryId;

    @Schema(description = "图书价格")
    private BigDecimal price;

    @NotNull(message = "总数量不能为空")
    @Schema(description = "图书总数量", required = true)
    private Integer totalNum;

    @Schema(description = "馆藏位置")
    private String location;

    @Schema(description = "封面图片地址")
    private String cover;

    @Schema(description = "图书简介")
    private String bookDesc;
}
