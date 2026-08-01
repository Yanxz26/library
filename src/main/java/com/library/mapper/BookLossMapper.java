package com.library.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.library.entity.BookLoss;
import org.apache.ibatis.annotations.Mapper;

/**
 * 图书损耗 Mapper
 *
 * @author Library Team
 */
@Mapper
public interface BookLossMapper extends BaseMapper<BookLoss> {
}
