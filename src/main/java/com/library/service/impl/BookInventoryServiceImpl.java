package com.library.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.common.exception.BusinessException;
import com.library.common.result.PageResult;
import com.library.common.result.Result;
import com.library.common.result.ResultCode;
import com.library.common.utils.ExcelUtil;
import com.library.dto.InventorySaveDTO;
import com.library.entity.BookInfo;
import com.library.entity.BookInventory;
import com.library.mapper.BookInfoMapper;
import com.library.mapper.BookInventoryMapper;
import com.library.service.BookInventoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 图书盘点服务实现
 *
 * @author Library Team
 */
@Slf4j
@Service
public class BookInventoryServiceImpl implements BookInventoryService {

    @Autowired
    private BookInventoryMapper bookInventoryMapper;

    @Autowired
    private BookInfoMapper bookInfoMapper;

    @Override
    public PageResult<BookInventory> pageQuery(Long current, Long size) {
        return pageQuery(null, null, current, size);
    }

    @Override
    public PageResult<BookInventory> pageQuery(String bookName, Boolean hasDiff, Long current, Long size) {
        Page<BookInventory> page = new Page<>(current, size);
        LambdaQueryWrapper<BookInventory> wrapper = new LambdaQueryWrapper<>();
        
        if (bookName != null && !bookName.isEmpty()) {
            wrapper.inSql(BookInventory::getBookId,
                    "SELECT id FROM book_info WHERE book_name LIKE '%" + bookName + "%'");
        }
        if (hasDiff != null) {
            if (hasDiff) {
                wrapper.ne(BookInventory::getDiffNum, 0);
            } else {
                wrapper.eq(BookInventory::getDiffNum, 0);
            }
        }
        
        wrapper.orderByDesc(BookInventory::getInventoryTime);
        Page<BookInventory> result = bookInventoryMapper.selectPage(page, wrapper);
        
        for (BookInventory inventory : result.getRecords()) {
            BookInfo book = bookInfoMapper.selectById(inventory.getBookId());
            if (book != null) {
                inventory.setBookName(book.getBookName());
                inventory.setAuthor(book.getAuthor());
            }
        }
        
        return PageResult.of(result.getCurrent(), result.getSize(), result.getTotal(), result.getRecords());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<?> doInventory(Long userId, InventorySaveDTO dto) {
        BookInfo book = bookInfoMapper.selectById(dto.getBookId());
        if (book == null) {
            throw new BusinessException(ResultCode.BOOK_NOT_FOUND);
        }

        int systemNum = book.getTotalNum();
        int actualNum = dto.getActualNum();
        int diffNum = actualNum - systemNum;

        BookInventory inventory = new BookInventory();
        inventory.setBookId(dto.getBookId());
        inventory.setSystemNum(systemNum);
        inventory.setActualNum(actualNum);
        inventory.setDiffNum(diffNum);
        inventory.setInventoryUser(userId);
        inventory.setInventoryTime(LocalDateTime.now());
        inventory.setRemark(dto.getRemark());
        bookInventoryMapper.insert(inventory);

        String diffMsg = diffNum == 0 ? "库存一致" : (diffNum > 0 ? "盘盈" + diffNum + "本" : "盘亏" + Math.abs(diffNum) + "本");
        log.info("图书盘点完成: bookId={}, systemNum={}, actualNum={}, diff={}, userId={}",
                dto.getBookId(), systemNum, actualNum, diffNum, userId);

        return Result.ok("盘点完成，" + diffMsg);
    }

    @Override
    public void exportInventory(HttpServletResponse response) {
        PageResult<BookInventory> pageResult = pageQuery(1L, 999999L);
        String[] headers = {"图书ID", "系统库存", "实际库存", "差异数量", "盘点人ID", "盘点时间", "备注"};
        String[] fields = {"bookId", "systemNum", "actualNum", "diffNum",
                "inventoryUser", "inventoryTime", "remark"};
        ExcelUtil.export(response, "盘点记录", pageResult.getRecords(), headers, fields);
    }
}
