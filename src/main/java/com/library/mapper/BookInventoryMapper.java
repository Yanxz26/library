package com.library.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.library.entity.BookInventory;
import org.apache.ibatis.annotations.Mapper;

/**
 * 图书盘点 Mapper
 *
 * @author Library Team
 */
@Mapper
public interface BookInventoryMapper extends BaseMapper<BookInventory> {
}
