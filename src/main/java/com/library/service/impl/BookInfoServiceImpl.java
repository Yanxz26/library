package com.library.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.library.common.exception.BusinessException;
import com.library.common.result.PageResult;
import com.library.common.result.Result;
import com.library.common.result.ResultCode;
import com.library.common.utils.ExcelUtil;
import com.library.dto.BookQueryDTO;
import com.library.dto.BookSaveDTO;
import com.library.entity.BookBorrow;
import com.library.entity.BookInfo;
import com.library.entity.BookReserve;
import com.library.mapper.BookBorrowMapper;
import com.library.mapper.BookInfoMapper;
import com.library.mapper.BookReserveMapper;
import com.library.service.BookInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 图书信息管理服务实现
 *
 * @author Library Team
 */
@Slf4j
@Service
public class BookInfoServiceImpl extends ServiceImpl<BookInfoMapper, BookInfo> implements BookInfoService {

    @Autowired
    private BookInfoMapper bookInfoMapper;

    @Autowired
    private BookBorrowMapper bookBorrowMapper;

    @Autowired
    private BookReserveMapper bookReserveMapper;

    @Override
    public PageResult<BookInfo> pageQuery(BookQueryDTO queryDTO) {
        Page<BookInfo> page = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
        LambdaQueryWrapper<BookInfo> wrapper = new LambdaQueryWrapper<>();

        if (StrUtil.isNotBlank(queryDTO.getBookName())) {
            wrapper.like(BookInfo::getBookName, queryDTO.getBookName());
        }
        if (StrUtil.isNotBlank(queryDTO.getAuthor())) {
            wrapper.like(BookInfo::getAuthor, queryDTO.getAuthor());
        }
        if (StrUtil.isNotBlank(queryDTO.getIsbn())) {
            wrapper.eq(BookInfo::getIsbn, queryDTO.getIsbn());
        }
        if (StrUtil.isNotBlank(queryDTO.getBookNo())) {
            wrapper.eq(BookInfo::getBookNo, queryDTO.getBookNo());
        }
        if (queryDTO.getCategoryId() != null) {
            wrapper.eq(BookInfo::getCategoryId, queryDTO.getCategoryId());
        }
        if (StrUtil.isNotBlank(queryDTO.getPublisher())) {
            wrapper.like(BookInfo::getPublisher, queryDTO.getPublisher());
        }
        if (queryDTO.getStatus() != null) {
            wrapper.eq(BookInfo::getStatus, queryDTO.getStatus());
        }

        // 排序处理
        String sortType = queryDTO.getSortType();
        if ("hot".equals(sortType)) {
            wrapper.orderByDesc(BookInfo::getTotalNum); // 简化处理，实际应按借阅次数排序
        } else if ("new".equals(sortType)) {
            wrapper.orderByDesc(BookInfo::getCreateTime);
        } else if ("available".equals(sortType)) {
            wrapper.gt(BookInfo::getRemainNum, 0).orderByDesc(BookInfo::getRemainNum);
        } else {
            wrapper.orderByDesc(BookInfo::getCreateTime);
        }

        Page<BookInfo> result = bookInfoMapper.selectPage(page, wrapper);
        return PageResult.of(result.getCurrent(), result.getSize(),
                result.getTotal(), result.getRecords());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<?> addBook(BookSaveDTO dto) {
        // 检查ISBN是否已存在
        Long count = bookInfoMapper.selectCount(
                new LambdaQueryWrapper<BookInfo>().eq(BookInfo::getIsbn, dto.getIsbn()));
        if (count > 0) {
            throw new BusinessException(2005, "该ISBN图书已存在");
        }

        BookInfo book = new BookInfo();
        BeanUtils.copyProperties(dto, book);
        // 剩余库存默认等于总数量
        book.setRemainNum(dto.getTotalNum());
        book.setStatus(1); // 默认上架状态

        // 如果图书编号为空，自动生成
        if (StrUtil.isBlank(book.getBookNo())) {
            Long maxNum = bookInfoMapper.selectMaxBookNoNum();
            book.setBookNo(String.format("B%04d", (maxNum != null ? maxNum : 0) + 1));
        }

        bookInfoMapper.insert(book);
        log.info("新增图书成功: {}, isbn={}", book.getBookName(), book.getIsbn());
        return Result.ok("新增图书成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<?> updateBook(BookSaveDTO dto) {
        BookInfo book = bookInfoMapper.selectById(dto.getId());
        if (book == null) {
            throw new BusinessException(ResultCode.BOOK_NOT_FOUND);
        }

        int oldTotal = book.getTotalNum();
        BeanUtils.copyProperties(dto, book);

        if (dto.getTotalNum() != null && dto.getTotalNum() > oldTotal) {
            int addNum = dto.getTotalNum() - oldTotal;
            book.setRemainNum(book.getRemainNum() + addNum);
        }

        bookInfoMapper.updateById(book);
        log.info("修改图书信息成功: id={}", book.getId());
        return Result.ok("修改图书成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<?> offShelf(Long bookId, Long userId) {
        BookInfo book = bookInfoMapper.selectById(bookId);
        if (book == null) {
            throw new BusinessException(ResultCode.BOOK_NOT_FOUND);
        }

        // 检查是否有未归还订单
        Long borrowCount = bookBorrowMapper.selectCount(
                new LambdaQueryWrapper<BookBorrow>()
                        .eq(BookBorrow::getBookId, bookId)
                        .eq(BookBorrow::getBorrowStatus, 1));
        if (borrowCount > 0) {
            throw new BusinessException(2006, "该图书存在未归还借阅订单，无法下架");
        }

        // 检查是否有有效预约
        Long reserveCount = bookReserveMapper.selectCount(
                new LambdaQueryWrapper<BookReserve>()
                        .eq(BookReserve::getBookId, bookId)
                        .eq(BookReserve::getReserveStatus, 1));
        if (reserveCount > 0) {
            throw new BusinessException(2007, "该图书存在有效预约订单，无法下架");
        }

        book.setStatus(0);
        bookInfoMapper.updateById(book);
        log.info("图书下架成功: bookId={}, operator={}", bookId, userId);
        return Result.ok("图书已下架");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<?> onShelf(Long bookId) {
        BookInfo book = bookInfoMapper.selectById(bookId);
        if (book == null) {
            throw new BusinessException(ResultCode.BOOK_NOT_FOUND);
        }
        book.setStatus(1);
        bookInfoMapper.updateById(book);
        return Result.ok("图书已上架");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<?> deleteBook(Long bookId) {
        BookInfo book = bookInfoMapper.selectById(bookId);
        if (book == null) {
            throw new BusinessException(ResultCode.BOOK_NOT_FOUND);
        }
        bookInfoMapper.deleteById(bookId);
        log.info("删除图书成功: bookId={}", bookId);
        return Result.ok("删除图书成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<?> addStock(Long bookId, Integer addNum) {
        if (addNum <= 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "增补数量必须大于0");
        }
        BookInfo book = bookInfoMapper.selectById(bookId);
        if (book == null) {
            throw new BusinessException(ResultCode.BOOK_NOT_FOUND);
        }
        book.setTotalNum(book.getTotalNum() + addNum);
        book.setRemainNum(book.getRemainNum() + addNum);
        bookInfoMapper.updateById(book);
        log.info("图书库存增补: bookId={}, addNum={}", bookId, addNum);
        return Result.ok("库存增补成功，增加" + addNum + "本");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<?> batchImport(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessException(ResultCode.FILE_UPLOAD_ERROR, "文件不能为空");
        }

        try {
            List<List<String>> rows = ExcelUtil.read(file.getInputStream());
            if (rows.size() < 2) {
                throw new BusinessException(ResultCode.FILE_FORMAT_ERROR, "Excel文件无数据，请确保有表头和至少一条数据");
            }

            int successCount = 0;
            List<String> errorList = new ArrayList<>();

            for (int i = 1; i < rows.size(); i++) {
                try {
                    List<String> row = rows.get(i);
                    
                    boolean allEmpty = true;
                    for (String cell : row) {
                        if (StrUtil.isNotBlank(cell)) {
                            allEmpty = false;
                            break;
                        }
                    }
                    if (allEmpty) {
                        continue;
                    }

                    if (row.size() < 5) {
                        errorList.add("第" + (i + 1) + "行: 数据不完整，至少需要5列（图书编号、ISBN、书名、作者、出版社）");
                        continue;
                    }

                    String bookNo = row.get(0).trim();
                    String isbn = row.get(1).trim();
                    String bookName = row.get(2).trim();
                    String author = row.get(3).trim();
                    String publisher = row.get(4).trim();
                    String totalNumStr = row.size() > 5 ? row.get(5).trim() : "1";
                    String priceStr = row.size() > 6 ? row.get(6).trim() : "0";
                    String categoryIdStr = row.size() > 7 ? row.get(7).trim() : "1";

                    if (StrUtil.isBlank(bookNo)) {
                        errorList.add("第" + (i + 1) + "行: 图书编号不能为空");
                        continue;
                    }
                    if (StrUtil.isBlank(isbn)) {
                        errorList.add("第" + (i + 1) + "行: ISBN不能为空");
                        continue;
                    }
                    if (StrUtil.isBlank(bookName)) {
                        errorList.add("第" + (i + 1) + "行: 书名不能为空");
                        continue;
                    }
                    if (StrUtil.isBlank(author)) {
                        errorList.add("第" + (i + 1) + "行: 作者不能为空");
                        continue;
                    }
                    if (StrUtil.isBlank(publisher)) {
                        errorList.add("第" + (i + 1) + "行: 出版社不能为空");
                        continue;
                    }

                    int totalNum;
                    try {
                        String cleanNumStr = totalNumStr.replaceAll("[^\\d.]", "").trim();
                        if (cleanNumStr.isEmpty()) {
                            totalNum = 1;
                        } else if (cleanNumStr.contains(".")) {
                            totalNum = (int) Math.round(Double.parseDouble(cleanNumStr));
                        } else {
                            totalNum = Integer.parseInt(cleanNumStr);
                        }
                    } catch (NumberFormatException e) {
                        errorList.add("第" + (i + 1) + "行: 数量格式错误");
                        continue;
                    }

                    BigDecimal price;
                    try {
                        String cleanPriceStr = priceStr.replaceAll("[^\\d.-]", "").trim();
                        if (cleanPriceStr.isEmpty()) {
                            price = BigDecimal.ZERO;
                        } else {
                            price = new BigDecimal(cleanPriceStr);
                        }
                    } catch (NumberFormatException e) {
                        errorList.add("第" + (i + 1) + "行: 价格格式错误");
                        continue;
                    }

                    Long categoryId;
                    try {
                        categoryId = Long.parseLong(categoryIdStr);
                    } catch (NumberFormatException e) {
                        errorList.add("第" + (i + 1) + "行: 分类ID格式错误");
                        continue;
                    }

                    BookInfo existBook = bookInfoMapper.selectOne(
                            new LambdaQueryWrapper<BookInfo>()
                                    .eq(BookInfo::getIsbn, isbn));
                    if (existBook != null) {
                        errorList.add("第" + (i + 1) + "行: ISBN已存在，跳过");
                        continue;
                    }

                    BookInfo book = new BookInfo();
                    book.setBookNo(bookNo);
                    book.setIsbn(isbn);
                    book.setBookName(bookName);
                    book.setAuthor(author);
                    book.setPublisher(publisher);
                    book.setTotalNum(totalNum);
                    book.setRemainNum(totalNum);
                    book.setPrice(price);
                    book.setStatus(1);
                    book.setCategoryId(categoryId);

                    bookInfoMapper.insert(book);
                    successCount++;
                } catch (Exception e) {
                    errorList.add("第" + (i + 1) + "行: " + e.getMessage());
                }
            }

            Map<String, Object> result = new HashMap<>();
            result.put("successCount", successCount);
            result.put("errorCount", errorList.size());
            result.put("errors", errorList);

            String msg = "导入完成，成功" + successCount + "条";
            if (!errorList.isEmpty()) {
                msg += "，失败" + errorList.size() + "条";
            }
            result.put("message", msg);

            return Result.ok(result);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("批量导入图书失败", e);
            throw new BusinessException(ResultCode.FILE_FORMAT_ERROR, "请检查Excel格式，支持.xlsx和.xls格式");
        }
    }

    @Override
    public void exportBooks(BookQueryDTO queryDTO, HttpServletResponse response) {
        PageResult<BookInfo> pageResult = pageQuery(queryDTO);
        String[] headers = {"图书编号", "ISBN", "书名", "作者", "出版社", "价格", "总数量", "剩余库存", "状态", "创建时间"};
        String[] fields = {"bookNo", "isbn", "bookName", "author", "publisher", "price",
                "totalNum", "remainNum", "status", "createTime"};
        ExcelUtil.export(response, "图书数据", pageResult.getRecords(), headers, fields);
    }

    @Override
    public Result<?> getBookDetail(Long bookId) {
        BookInfo book = bookInfoMapper.selectById(bookId);
        if (book == null) {
            throw new BusinessException(ResultCode.BOOK_NOT_FOUND);
        }
        return Result.ok(book);
    }

    @Override
    public Result<?> getHotBooks() {
        // 简化处理：按总数量排序（实际应按借阅次数）
        List<BookInfo> hotBooks = bookInfoMapper.selectList(
                new LambdaQueryWrapper<BookInfo>()
                        .eq(BookInfo::getStatus, 1)
                        .orderByDesc(BookInfo::getTotalNum)
                        .last("LIMIT 10"));
        return Result.ok(hotBooks);
    }

    @Override
    public Result<?> getNewBooks() {
        List<BookInfo> newBooks = bookInfoMapper.selectList(
                new LambdaQueryWrapper<BookInfo>()
                        .eq(BookInfo::getStatus, 1)
                        .orderByDesc(BookInfo::getCreateTime)
                        .last("LIMIT 10"));
        return Result.ok(newBooks);
    }

    @Override
    public Result<?> uploadCover(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessException(ResultCode.FILE_UPLOAD_ERROR, "文件不能为空");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.toLowerCase().matches(".*\\.(jpg|jpeg|png|gif)$")) {
            throw new BusinessException(ResultCode.FILE_FORMAT_ERROR, "只支持jpg、jpeg、png、gif格式的图片");
        }

        // 使用项目根目录作为上传目录的基准
        String projectRoot = System.getProperty("user.dir");
        log.info("项目根目录: {}", projectRoot);
        
        String uploadDir = projectRoot + "/uploads/book-covers/";
        java.io.File dir = new java.io.File(uploadDir);
        if (!dir.exists()) {
            boolean created = dir.mkdirs();
            if (created) {
                log.info("创建上传目录成功: {}", dir.getAbsolutePath());
            } else {
                log.error("创建上传目录失败: {}", dir.getAbsolutePath());
                throw new BusinessException(ResultCode.FILE_UPLOAD_ERROR, "无法创建上传目录");
            }
        }

        String filename = System.currentTimeMillis() + "_" + originalFilename;
        java.io.File targetFile = new java.io.File(uploadDir, filename);
        
        try {
            file.transferTo(targetFile);
            log.info("文件上传成功: {}", targetFile.getAbsolutePath());
            String coverUrl = "/api/uploads/book-covers/" + filename;
            return Result.ok(coverUrl);
        } catch (Exception e) {
            log.error("上传封面失败，目标文件: {}", targetFile.getAbsolutePath(), e);
            throw new BusinessException(ResultCode.FILE_UPLOAD_ERROR, "上传失败: " + e.getMessage());
        }
    }
}
