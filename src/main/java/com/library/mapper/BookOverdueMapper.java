package com.library.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.library.entity.BookOverdue;
import org.apache.ibatis.annotations.Mapper;

/**
 * 逾期记录 Mapper
 *
 * @author Library Team
 */
@Mapper
public interface BookOverdueMapper extends BaseMapper<BookOverdue> {
}
