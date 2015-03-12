package com.mtrade.consumer;

import org.springframework.integration.annotation.Header;

import com.mtrade.common.model.TradeRequest;

public interface RequestProcessorGateway {

    void process(TradeRequest tradeRequest, @Header("transactionId") String transactionId);
}
