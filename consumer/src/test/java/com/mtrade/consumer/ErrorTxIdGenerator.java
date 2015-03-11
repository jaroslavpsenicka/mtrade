package com.mtrade.consumer;

import java.util.UUID;

/**
 * @author jaroslav.psenicka@gmail.com
 */
public class ErrorTxIdGenerator extends TransactionIdGenerator {

    public String generateId() {
        throw new IllegalStateException("expected error");
    }

}
