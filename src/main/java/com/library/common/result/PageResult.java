package com.library.common.result;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 分页返回结果封装
 *
 * @author Library Team
 */
@Data
public class PageResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 当前页码 */
    private Long current;

    /** 每页条数 */
    private Long size;

    /** 总条数 */
    private Long total;

    /** 总页数 */
    private Long pages;

    /** 数据列表 */
    private List<T> records;

    private PageResult() {}

    public static <T> PageResult<T> of(Long current, Long size, Long total, List<T> records) {
        PageResult<T> result = new PageResult<>();
        result.setCurrent(current);
        result.setSize(size);
        result.setTotal(total);
        result.setPages(total % size == 0 ? total / size : total / size + 1);
        result.setRecords(records);
        return result;
    }
}
