package com.library.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.library.common.exception.BusinessException;
import com.library.common.result.Result;
import com.library.common.result.ResultCode;
import com.library.entity.BookCategory;
import com.library.entity.BookInfo;
import com.library.mapper.BookCategoryMapper;
import com.library.mapper.BookInfoMapper;
import com.library.service.BookCategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 图书分类服务实现
 *
 * @author Library Team
 */
@Slf4j
@Service
public class BookCategoryServiceImpl extends ServiceImpl<BookCategoryMapper, BookCategory>
        implements BookCategoryService {

    @Autowired
    private BookCategoryMapper bookCategoryMapper;

    @Autowired
    private BookInfoMapper bookInfoMapper;

    @Override
    public List<BookCategory> getCategoryTree() {
        // 查询所有有效分类
        List<BookCategory> allList = bookCategoryMapper.selectList(
                new LambdaQueryWrapper<BookCategory>()
                        .eq(BookCategory::getStatus, 1)
                        .orderByAsc(BookCategory::getSort));

        // 找出所有一级分类
        List<BookCategory> treeList = allList.stream()
                .filter(c -> c.getParentId() == 0)
                .collect(Collectors.toList());

        // 为每个一级分类设置子分类
        for (BookCategory parent : treeList) {
            List<BookCategory> children = allList.stream()
                    .filter(c -> c.getParentId().equals(parent.getId()))
                    .collect(Collectors.toList());
            // 子分类暂时通过前端自行处理
        }

        return treeList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<?> addCategory(BookCategory category) {
        category.setSort(category.getSort() != null ? category.getSort() : 0);
        category.setStatus(category.getStatus() != null ? category.getStatus() : 1);
        bookCategoryMapper.insert(category);
        log.info("新增图书分类: {}", category.getCategoryName());
        return Result.ok("新增分类成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<?> updateCategory(BookCategory category) {
        BookCategory existCategory = bookCategoryMapper.selectById(category.getId());
        if (existCategory == null) {
            throw new BusinessException(ResultCode.CATEGORY_NOT_FOUND);
        }
        bookCategoryMapper.updateById(category);
        log.info("修改图书分类: id={}", category.getId());
        return Result.ok("修改分类成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<?> deleteCategory(Long id) {
        BookCategory category = bookCategoryMapper.selectById(id);
        if (category == null) {
            throw new BusinessException(ResultCode.CATEGORY_NOT_FOUND);
        }

        // 检查分类下是否有图书
        Long bookCount = bookInfoMapper.selectCount(
                new LambdaQueryWrapper<BookInfo>()
                        .eq(BookInfo::getCategoryId, id));
        if (bookCount > 0) {
            throw new BusinessException(ResultCode.CATEGORY_HAS_BOOKS);
        }

        // 检查是否有子分类
        Long childCount = bookCategoryMapper.selectCount(
                new LambdaQueryWrapper<BookCategory>()
                        .eq(BookCategory::getParentId, id));
        if (childCount > 0) {
            throw new BusinessException(5001, "分类下存在子分类，无法删除");
        }

        bookCategoryMapper.deleteById(id);
        log.info("删除图书分类: id={}", id);
        return Result.ok("删除分类成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<?> updateStatus(Long id, Integer status) {
        BookCategory category = bookCategoryMapper.selectById(id);
        if (category == null) {
            throw new BusinessException(ResultCode.CATEGORY_NOT_FOUND);
        }
        category.setStatus(status);
        bookCategoryMapper.updateById(category);
        return Result.ok(status == 1 ? "分类已启用" : "分类已禁用");
    }
}
