package com.library.common.result;

import lombok.Getter;

/**
 * 统一返回状态码枚举
 *
 * @author Library Team
 */
@Getter
public enum ResultCode {

    SUCCESS(200, "操作成功"),
    BAD_REQUEST(400, "参数错误"),
    UNAUTHORIZED(401, "未授权，请重新登录"),
    FORBIDDEN(403, "无权限访问"),
    NOT_FOUND(404, "数据不存在"),
    INTERNAL_ERROR(500, "服务器内部异常"),

    // 业务状态码
    USER_ACCOUNT_EXIST(1001, "账号已存在"),
    USER_NOT_FOUND(1002, "用户不存在"),
    USER_DISABLED(1003, "账号已被禁用"),
    PASSWORD_ERROR(1004, "密码错误"),
    OLD_PASSWORD_ERROR(1005, "原密码错误"),
    LOGIN_EXPIRED(1006, "登录已过期，请重新登录"),

    BOOK_NOT_FOUND(2001, "图书不存在"),
    BOOK_NOT_AVAILABLE(2002, "图书库存不足"),
    BOOK_OFF_SHELF(2003, "图书已下架，无法借阅"),
    BOOK_BORROW_EXIST(2004, "该图书已有未归还记录"),

    BORROW_LIMIT_EXCEED(3001, "超出最大借阅数量限制"),
    BORROW_OVERDUE_EXIST(3002, "存在未处理逾期记录，无法借阅"),
    BORROW_ALREADY_RENEWED(3003, "该图书已续借过，无法再次续借"),
    BORROW_OVERDUE_NO_RENEW(3004, "逾期图书无法续借"),
    BORROW_EXPIRED_NO_RENEW(3005, "已到期图书无法续借"),
    BORROW_NOT_FOUND(3006, "借阅记录不存在"),
    BORROW_ALREADY_RETURNED(3007, "该图书已归还"),

    RESERVE_ALREADY_EXIST(4001, "您已预约过该图书"),
    RESERVE_EXPIRED(4002, "预约已失效"),
    RESERVE_NOT_FOUND(4003, "预约记录不存在"),

    CATEGORY_HAS_BOOKS(5001, "分类下存在图书，无法删除"),
    CATEGORY_NOT_FOUND(5002, "分类不存在"),

    FINE_NOT_PAID(6001, "存在未缴纳罚款"),
    OVERDUE_NOT_FOUND(6002, "逾期记录不存在"),

    CONFIG_KEY_EXIST(7001, "配置键已存在"),
    CONFIG_NOT_FOUND(7002, "配置不存在"),

    FILE_UPLOAD_ERROR(8001, "文件上传失败"),
    FILE_FORMAT_ERROR(8002, "文件格式错误"),
    FILE_EXPORT_ERROR(8003, "文件导出失败");

    private final Integer code;
    private final String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
