package com.library.service;

import com.library.common.result.PageResult;
import com.library.common.result.Result;
import com.library.dto.LossSaveDTO;
import com.library.entity.BookLoss;

import javax.servlet.http.HttpServletResponse;

/**
 * 图书损耗服务接口
 *
 * @author Library Team
 */
public interface BookLossService {

    /**
     * 分页查询损耗记录
     */
    PageResult<BookLoss> pageQuery(Long current, Long size);

    /**
     * 分页查询损耗记录（管理员）
     */
    PageResult<BookLoss> pageQuery(String bookName, Integer lossType, Long current, Long size);

    /**
     * 登记损耗
     */
    Result<?> registerLoss(Long userId, LossSaveDTO dto);

    /**
     * 导出损耗记录
     */
    void exportLoss(HttpServletResponse response);
}
