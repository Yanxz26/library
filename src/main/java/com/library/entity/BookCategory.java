package com.library.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 图书分类表 book_category
 *
 * @author Library Team
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("book_category")
public class BookCategory extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 分类名称 */
    private String categoryName;

    /** 父分类ID（0为一级分类） */
    private Long parentId;

    /** 排序权重 */
    private Integer sort;

    /** 状态（0禁用，1正常） */
    private Integer status;
}
