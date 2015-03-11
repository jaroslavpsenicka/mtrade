package com.mtrade.model;

/**
 * @author jaroslav.psenicka@gmail.com
 */
public class TradeResponse {

    private String txId;

    public TradeResponse() {
    }

    public TradeResponse(String txId) {
        this.txId = txId;
    }

    public String getTxId() {
        return txId;
    }

    public void setTxId(String txId) {
        this.txId = txId;
    }
}
