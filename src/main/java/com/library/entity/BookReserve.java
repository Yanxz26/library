package com.library.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 图书预约记录表 book_reserve
 *
 * @author Library Team
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("book_reserve")
public class BookReserve extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 预约用户ID */
    private Long userId;

    /** 预约图书ID */
    private Long bookId;

    /** 预约时间 */
    private LocalDateTime reserveTime;

    /** 预约失效时间 */
    private LocalDateTime expireTime;

    /** 状态（1待生效，2已完成，3已失效） */
    private Integer reserveStatus;

    /** 用户姓名（非数据库字段） */
    @TableField(exist = false)
    private String userName;

    /** 图书名称（非数据库字段） */
    @TableField(exist = false)
    private String bookName;

    /** 作者（非数据库字段） */
    @TableField(exist = false)
    private String author;

    /** 出版社（非数据库字段） */
    @TableField(exist = false)
    private String publisher;
}
