package com.netdisk.advice;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class TransactionManagerConfig {
    
    @Bean
    public PlatformTransactionManager transactionManager() {
        // 返回自定义的无操作事务管理器
        return new NoOpTransactionManager();
    }
}
