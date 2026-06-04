package com.printims;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

/**
 * 印刷行业综合管理系统启动类。
 */
@SpringBootApplication
@EnableCaching
@MapperScan("com.printims.module.**.mapper")
public class PrintImsApplication {

    public static void main(String[] args) {
        SpringApplication.run(PrintImsApplication.class, args);
    }
}
