package com.library.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.library.entity.BookBorrow;
import org.apache.ibatis.annotations.Mapper;

/**
 * 图书借阅记录 Mapper
 *
 * @author Library Team
 */
@Mapper
public interface BookBorrowMapper extends BaseMapper<BookBorrow> {
}
