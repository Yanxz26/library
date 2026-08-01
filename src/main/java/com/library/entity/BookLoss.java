package com.library.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 图书损耗记录表 book_loss
 *
 * @author Library Team
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("book_loss")
public class BookLoss extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 图书ID */
    private Long bookId;

    /** 损耗数量 */
    private Integer lossNum;

    /** 损耗类型（1破损，2丢失，3报废） */
    private Integer lossType;

    /** 损耗原因 */
    private String lossReason;

    /** 登记人ID */
    private Long recordUser;

    /** 登记时间 */
    private LocalDateTime recordTime;

    /** 图书名称（非数据库字段） */
    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private String bookName;

    /** 作者（非数据库字段） */
    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private String author;
}
