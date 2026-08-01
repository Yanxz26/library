package com.library;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * 校园图书管理系统 - 启动类
 *
 * @author Library Team
 * @since 1.0.0
 */
@SpringBootApplication
@EnableTransactionManagement
@EnableScheduling
@EnableAsync
public class LibraryApplication {

    public static void main(String[] args) {
        SpringApplication.run(LibraryApplication.class, args);
        System.out.println("========================================");
        System.out.println("  校园图书管理系统后端服务启动成功!");
        System.out.println("  Swagger文档: http://localhost:8080/api/swagger-ui.html");
        System.out.println("========================================");
    }
}
