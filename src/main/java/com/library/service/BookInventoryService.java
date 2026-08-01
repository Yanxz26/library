package com.library.service;

import com.library.common.result.PageResult;
import com.library.common.result.Result;
import com.library.dto.InventorySaveDTO;
import com.library.entity.BookInventory;

import javax.servlet.http.HttpServletResponse;

/**
 * 图书盘点服务接口
 *
 * @author Library Team
 */
public interface BookInventoryService {

    /**
     * 分页查询盘点记录
     */
    PageResult<BookInventory> pageQuery(Long current, Long size);

    /**
     * 分页查询盘点记录（管理员）
     */
    PageResult<BookInventory> pageQuery(String bookName, Boolean hasDiff, Long current, Long size);

    /**
     * 执行盘点
     */
    Result<?> doInventory(Long userId, InventorySaveDTO dto);

    /**
     * 导出盘点记录
     */
    void exportInventory(HttpServletResponse response);
}
