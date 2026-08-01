package com.library.common.annotation;

import java.lang.annotation.*;

/**
 * 自定义操作日志注解
 *
 * @author Library Team
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Log {

    /** 操作类型: 1操作日志, 2业务日志, 3异常日志 */
    int type() default 1;

    /** 操作描述 */
    String value() default "";
}
