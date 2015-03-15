package com.mtrade.common.model;

import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.constraints.NotNull;

/**
 * @author jaroslav.psenicka@gmail.com
 */
public class TradeResponse {

    @NotNull
    private String txId;

    public TradeResponse() {
    }

    public TradeResponse(String txId) {
        this.txId = txId;
    }

    public String getTxId() {
        return txId;
    }

}
