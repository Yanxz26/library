package com.library;

import com.library.common.utils.Md5Util;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class LibraryApplicationTests {

    @Test
    void contextLoads() {
        System.out.println("校园图书管理系统启动测试通过");
    }

    @Test
    void testPasswordEncode() {
        // 测试MD5加密
        String password = "admin123";
        String encoded = Md5Util.encode(password);
        System.out.println("原始密码: " + password);
        System.out.println("加密后: " + encoded);
        System.out.println("验证通过: " + Md5Util.verify(password, encoded));
    }
}
