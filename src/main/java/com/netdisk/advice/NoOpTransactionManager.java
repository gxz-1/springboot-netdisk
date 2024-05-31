package com.netdisk.advice;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionStatus;

public class NoOpTransactionManager implements PlatformTransactionManager {
    
    @Override
    public TransactionStatus getTransaction(TransactionDefinition definition) throws TransactionException {
        return new DefaultTransactionStatus(
            null, false, true, definition.isReadOnly(), true, null);
    }

    @Override
    public void commit(TransactionStatus status) throws TransactionException {
        // 无操作
    }

    @Override
    public void rollback(TransactionStatus status) throws TransactionException {
        // 无操作
    }
}
