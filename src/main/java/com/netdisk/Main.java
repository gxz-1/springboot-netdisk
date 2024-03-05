package com.netdisk;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableAsync //异步调用
@EnableScheduling //定时任务
@EnableTransactionManagement //事务管理
@MapperScan("com.netdisk.mappers")
public class Main {
    public static void main(String[] args) {
        SpringApplication.run(Main.class,args);
    }
}
