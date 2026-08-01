package com.library.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.library.common.result.PageResult;
import com.library.common.result.Result;
import com.library.dto.BookQueryDTO;
import com.library.dto.BookSaveDTO;
import com.library.entity.BookInfo;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;

/**
 * 图书信息管理服务接口
 *
 * @author Library Team
 */
public interface BookInfoService extends IService<BookInfo> {

    /**
     * 分页查询图书列表
     */
    PageResult<BookInfo> pageQuery(BookQueryDTO queryDTO);

    /**
     * 新增图书
     */
    Result<?> addBook(BookSaveDTO dto);

    /**
     * 修改图书
     */
    Result<?> updateBook(BookSaveDTO dto);

    /**
     * 图书下架
     */
    Result<?> offShelf(Long bookId, Long userId);

    /**
     * 图书上架
     */
    Result<?> onShelf(Long bookId);

    /**
     * 删除图书（逻辑删除）
     */
    Result<?> deleteBook(Long bookId);

    /**
     * 库存增补
     */
    Result<?> addStock(Long bookId, Integer addNum);

    /**
     * 批量导入图书
     */
    Result<?> batchImport(MultipartFile file);

    /**
     * 导出图书数据
     */
    void exportBooks(BookQueryDTO queryDTO, HttpServletResponse response);

    /**
     * 获取图书详情
     */
    Result<?> getBookDetail(Long bookId);

    /**
     * 获取热门图书TOP10
     */
    Result<?> getHotBooks();

    /**
     * 获取新书上架
     */
    Result<?> getNewBooks();

    /**
     * 上传图书封面
     */
    Result<?> uploadCover(MultipartFile file);
}
