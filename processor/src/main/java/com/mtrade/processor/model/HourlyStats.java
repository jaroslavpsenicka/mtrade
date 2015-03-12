package com.mtrade.processor.model;

import org.springframework.data.annotation.Id;

/**
 * @author jaroslav.psenicka@gmail.com
 */
public class HourlyStats {

    @Id
    private String countryCode;
    private int count;

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
