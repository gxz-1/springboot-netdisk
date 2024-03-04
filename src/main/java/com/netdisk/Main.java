package com.netdisk;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync //异步调用
@EnableScheduling //定时任务
public class Main {
    public static void main(String[] args) {
        SpringApplication.run(Main.class,args);
    }
}
