package com.library.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 图书借阅记录表 book_borrow
 *
 * @author Library Team
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("book_borrow")
public class BookBorrow extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 借阅用户ID */
    private Long userId;

    /** 图书ID */
    private Long bookId;

    /** 借阅时间 */
    private LocalDateTime borrowTime;

    /** 到期时间 */
    private LocalDateTime expireTime;

    /** 归还时间（为空表示未归还） */
    private LocalDateTime returnTime;

    /** 续借次数 */
    private Integer renewCount;

    /** 订单状态（1借阅中，2已归还，3已逾期） */
    private Integer borrowStatus;

    /** 图书名称（非持久化，用于返回数据） */
    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private String bookName;

    /** 作者（非持久化，用于返回数据） */
    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private String author;

    /** 出版社（非持久化，用于返回数据） */
    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private String publisher;

    /** ISBN（非持久化，用于返回数据） */
    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private String isbn;
}
