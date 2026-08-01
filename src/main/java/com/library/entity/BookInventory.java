package com.library.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 图书盘点记录表 book_inventory
 *
 * @author Library Team
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("book_inventory")
public class BookInventory extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 图书ID */
    private Long bookId;

    /** 系统库存数量 */
    private Integer systemNum;

    /** 实际盘点数量 */
    private Integer actualNum;

    /** 差异数量（正数盘盈，负数盘亏） */
    private Integer diffNum;

    /** 盘点人ID */
    private Long inventoryUser;

    /** 盘点时间 */
    private LocalDateTime inventoryTime;

    /** 盘点备注 */
    private String remark;

    /** 图书名称（非数据库字段） */
    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private String bookName;

    /** 作者（非数据库字段） */
    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private String author;
}
