package com.library.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 图书信息表 book_info
 *
 * @author Library Team
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("book_info")
public class BookInfo extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 图书编号 */
    private String bookNo;

    /** 图书ISBN编号 */
    private String isbn;

    /** 图书名称 */
    private String bookName;

    /** 作者 */
    private String author;

    /** 出版社 */
    private String publisher;

    /** 出版时间 */
    private LocalDate publishTime;

    /** 关联分类ID */
    private Long categoryId;

    /** 图书价格 */
    private BigDecimal price;

    /** 图书总数量 */
    private Integer totalNum;

    /** 剩余可借库存 */
    private Integer remainNum;

    /** 馆藏位置 */
    private String location;

    /** 封面图片地址 */
    private String cover;

    /** 图书简介 */
    private String bookDesc;

    /** 状态（0下架，1正常） */
    private Integer status;
}
