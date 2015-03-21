package com.mtrade.common.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * @author jaroslav.psenicka@gmail.com
 */
public class ExchangeStats {

    @JsonIgnore
    private String id;

    @NotNull
    private String originatingCountry;

    @NotNull
    private String currencyFrom;

    @NotNull
    private String currencyTo;

    @NotNull
    private Integer count;

    @NotNull
    private Float amount;

    @NotNull
    private Date createDate;

    public ExchangeStats() {
    }

    public String getId() {
        return id;
    }

    public String getOriginatingCountry() {
        return originatingCountry;
    }

    public void setOriginatingCountry(String originatingCountry) {
        this.originatingCountry = originatingCountry;
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

    public Float getAmount() {
        return amount;
    }

    public void setAmount(Float amount) {
        this.amount = amount;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }
}
