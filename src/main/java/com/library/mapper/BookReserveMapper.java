package com.library.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.library.entity.BookReserve;
import org.apache.ibatis.annotations.Mapper;

/**
 * 图书预约 Mapper
 *
 * @author Library Team
 */
@Mapper
public interface BookReserveMapper extends BaseMapper<BookReserve> {
}
