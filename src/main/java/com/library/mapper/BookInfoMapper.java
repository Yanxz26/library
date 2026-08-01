package com.library.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.library.entity.BookInfo;
import org.apache.ibatis.annotations.Mapper;

/**
 * 图书信息 Mapper
 *
 * @author Library Team
 */
@Mapper
public interface BookInfoMapper extends BaseMapper<BookInfo> {

    Long selectMaxBookNoNum();
}
