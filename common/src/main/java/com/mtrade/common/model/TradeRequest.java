package com.mtrade.common.model;

import java.util.Date;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mtrade.common.annotation.CurrencyCode;
import com.mtrade.common.annotation.UnequalValues;
import de.malkusch.validation.constraints.Country;

/**
 * @author jaroslav.psenicka@gmail.com
 */
@UnequalValues(regex = "currency.*", message = "currency codes must not be equal")
public class TradeRequest {

    @NotNull
    @Size(min = 1, max = 128)
    private String userId;

    @NotNull
    @CurrencyCode
    private String currencyFrom;

    @NotNull
    @CurrencyCode
    private String currencyTo;

    @NotNull
    @Min(1)
    @Max(1000000)
    private Float amountSell;

    @NotNull
    @Min(1)
    @Max(1000000)
    private Float amountBuy;

    @Min(0)
    private Float rate;

    @NotNull
    @JsonFormat(locale = "EN", shape = JsonFormat.Shape.STRING, pattern = "yy-MMM-dd hh:mm:ss")
    private Date timePlaced;

    @NotNull
    @Country
    private String originatingCountry;

    @JsonIgnore
    private Date timeCreated;

    @JsonIgnore
    private String transactionId;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getCurrencyFrom() {
        return currencyFrom;
    }

    public void setCurrencyFrom(String currencyFrom) {
        this.currencyFrom = currencyFrom;
    }

    public String getCurrencyTo() {
        return currencyTo;
    }

    public void setCurrencyTo(String currencyTo) {
        this.currencyTo = currencyTo;
    }

    public Float getAmountSell() {
        return amountSell;
    }

    public void setAmountSell(Float amountSell) {
        this.amountSell = amountSell;
    }

    public Float getAmountBuy() {
        return amountBuy;
    }

    public void setAmountBuy(Float amountBuy) {
        this.amountBuy = amountBuy;
    }

    public Float getRate() {
        return rate;
    }

    public void setRate(Float rate) {
        this.rate = rate;
    }

    public String getOriginatingCountry() {
        return originatingCountry;
    }

    public void setOriginatingCountry(String originatingCountry) {
        this.originatingCountry = originatingCountry;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public Date getTimePlaced() {
        return timePlaced;
    }

    public void setTimePlaced(Date timePlaced) {
        this.timePlaced = timePlaced;
    }

    public Date getTimeCreated() {
        return timeCreated;
    }

    public void setTimeCreated(Date timeCreated) {
        this.timeCreated = timeCreated;
    }
}
