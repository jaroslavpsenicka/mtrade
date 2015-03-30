package com.mtrade.consumer;

import java.util.UUID;

import org.springframework.stereotype.Component;

@Component
public class TransactionIdGenerator {

    public String generateId() {
        return UUID.randomUUID().toString();
    }

}
