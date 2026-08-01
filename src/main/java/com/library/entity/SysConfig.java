package com.library.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 系统配置表 sys_config
 *
 * @author Library Team
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_config")
public class SysConfig extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 配置键名 */
    private String configKey;

    /** 配置值 */
    private String configValue;

    /** 配置名称 */
    private String configName;

    /** 配置描述 */
    private String remark;
}
