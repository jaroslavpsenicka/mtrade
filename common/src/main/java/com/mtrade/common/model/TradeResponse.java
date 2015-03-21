package com.mtrade.common.model;

import javax.validation.constraints.NotNull;

/**
 * @author jaroslav.psenicka@gmail.com
 */
public class TradeResponse {

    @NotNull
    private String txId;

    public TradeResponse(String txId) {
        this.txId = txId;
    }

    public String getTxId() {
        return txId;
    }

}
