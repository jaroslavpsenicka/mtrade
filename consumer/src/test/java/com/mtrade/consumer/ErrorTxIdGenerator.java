package com.mtrade.consumer;

/**
 * @author jaroslav.psenicka@gmail.com
 */
public class ErrorTxIdGenerator extends TransactionIdGenerator {

    public String generateId() {
        throw new IllegalStateException("expected error");
    }

}
