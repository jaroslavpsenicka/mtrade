package com.mtrade.processor.model;

import com.mtrade.processor.StatsCalculator;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * @author jaroslav.psenicka@gmail.com
 */
@Document
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
