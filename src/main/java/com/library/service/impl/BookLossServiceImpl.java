package com.library.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.common.exception.BusinessException;
import com.library.common.result.PageResult;
import com.library.common.result.Result;
import com.library.common.result.ResultCode;
import com.library.common.utils.ExcelUtil;
import com.library.dto.LossSaveDTO;
import com.library.entity.BookInfo;
import com.library.entity.BookLoss;
import com.library.mapper.BookInfoMapper;
import com.library.mapper.BookLossMapper;
import com.library.service.BookLossService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;

/**
 * 图书损耗服务实现
 *
 * @author Library Team
 */
@Slf4j
@Service
public class BookLossServiceImpl implements BookLossService {

    @Autowired
    private BookLossMapper bookLossMapper;

    @Autowired
    private BookInfoMapper bookInfoMapper;

    @Override
    public PageResult<BookLoss> pageQuery(Long current, Long size) {
        return pageQuery(null, null, current, size);
    }

    @Override
    public PageResult<BookLoss> pageQuery(String bookName, Integer lossType, Long current, Long size) {
        Page<BookLoss> page = new Page<>(current, size);
        LambdaQueryWrapper<BookLoss> wrapper = new LambdaQueryWrapper<>();
        
        if (bookName != null && !bookName.isEmpty()) {
            wrapper.inSql(BookLoss::getBookId,
                    "SELECT id FROM book_info WHERE book_name LIKE '%" + bookName + "%'");
        }
        if (lossType != null) {
            wrapper.eq(BookLoss::getLossType, lossType);
        }
        
        wrapper.orderByDesc(BookLoss::getRecordTime);
        Page<BookLoss> result = bookLossMapper.selectPage(page, wrapper);
        
        for (BookLoss loss : result.getRecords()) {
            BookInfo book = bookInfoMapper.selectById(loss.getBookId());
            if (book != null) {
                loss.setBookName(book.getBookName());
                loss.setAuthor(book.getAuthor());
            }
        }
        
        return PageResult.of(result.getCurrent(), result.getSize(), result.getTotal(), result.getRecords());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<?> registerLoss(Long userId, LossSaveDTO dto) {
        BookInfo book = bookInfoMapper.selectById(dto.getBookId());
        if (book == null) {
            throw new BusinessException(ResultCode.BOOK_NOT_FOUND);
        }
        if (dto.getLossNum() > book.getTotalNum()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "损耗数量不能超过总库存");
        }

        // 创建损耗记录
        BookLoss loss = new BookLoss();
        loss.setBookId(dto.getBookId());
        loss.setLossNum(dto.getLossNum());
        loss.setLossType(dto.getLossType());
        loss.setLossReason(dto.getLossReason());
        loss.setRecordUser(userId);
        loss.setRecordTime(LocalDateTime.now());
        bookLossMapper.insert(loss);

        // 更新图书库存
        book.setTotalNum(book.getTotalNum() - dto.getLossNum());
        book.setRemainNum(Math.max(0, book.getRemainNum() - dto.getLossNum()));
        bookInfoMapper.updateById(book);

        log.info("图书损耗登记: bookId={}, lossNum={}, lossType={}, userId={}",
                dto.getBookId(), dto.getLossNum(), dto.getLossType(), userId);

        return Result.ok("损耗登记成功");
    }

    @Override
    public void exportLoss(HttpServletResponse response) {
        PageResult<BookLoss> pageResult = pageQuery(1L, 999999L);
        String[] headers = {"图书ID", "损耗数量", "损耗类型", "损耗原因", "登记人ID", "登记时间"};
        String[] fields = {"bookId", "lossNum", "lossType", "lossReason", "recordUser", "recordTime"};
        ExcelUtil.export(response, "损耗记录", pageResult.getRecords(), headers, fields);
    }
}
