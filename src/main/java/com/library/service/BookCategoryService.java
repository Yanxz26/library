package com.library.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.library.common.result.Result;
import com.library.entity.BookCategory;

import java.util.List;

/**
 * 图书分类服务接口
 *
 * @author Library Team
 */
public interface BookCategoryService extends IService<BookCategory> {

    /**
     * 获取分类树（全部有效分类，含层级结构）
     */
    List<BookCategory> getCategoryTree();

    /**
     * 新增分类
     */
    Result<?> addCategory(BookCategory category);

    /**
     * 修改分类
     */
    Result<?> updateCategory(BookCategory category);

    /**
     * 删除分类（检查是否绑定图书）
     */
    Result<?> deleteCategory(Long id);

    /**
     * 更新分类状态
     */
    Result<?> updateStatus(Long id, Integer status);
}
