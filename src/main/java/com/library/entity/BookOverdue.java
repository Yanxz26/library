package com.library.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 逾期记录表 book_overdue
 *
 * @author Library Team
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("book_overdue")
public class BookOverdue extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 关联借阅订单ID */
    private Long borrowId;

    /** 用户ID */
    private Long userId;

    /** 图书ID */
    private Long bookId;

    /** 逾期天数 */
    private Integer overdueDays;

    /** 逾期罚款金额 */
    private BigDecimal fineMoney;

    /** 缴费状态（0未缴费，1已缴费） */
    private Integer payStatus;

    /** 缴费时间 */
    private LocalDateTime payTime;

    /** 用户姓名（非数据库字段） */
    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private String userName;

    /** 图书名称（非数据库字段） */
    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private String bookName;

    /** 作者（非数据库字段） */
    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private String author;
}
