package com.library.common.utils;

import cn.hutool.crypto.digest.MD5;

/**
 * MD5加密工具类
 *
 * @author Library Team
 */
public class Md5Util {

    private static final String SALT = "library_salt_2024";

    /**
     * MD5加密
     */
    public static String encode(String password) {
        return MD5.create().digestHex(password + SALT);
    }

    /**
     * 验证密码
     */
    public static boolean verify(String password, String encodedPassword) {
        return encode(password).equals(encodedPassword);
    }

    /**
     * 生成默认密码（学号/工号的后6位）
     */
    public static String generateDefaultPassword(String account) {
        if (account.length() >= 6) {
            return encode(account.substring(account.length() - 6));
        }
        return encode(account);
    }
}
