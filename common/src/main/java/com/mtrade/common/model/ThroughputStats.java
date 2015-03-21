package com.mtrade.common.model;

import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * @author jaroslav.psenicka@gmail.com
 */
public class ThroughputStats {

    private String id;

    @NotNull
    private String countryCode;

    @NotNull
    private Date createDate;

    private float count;

    public ThroughputStats() {
    }

    public ThroughputStats(String countryCode, Date createDate) {
        this.countryCode = countryCode;
        this.createDate = createDate;
    }

    public ThroughputStats(String countryCode, Date createDate, float count) {
        this.countryCode = countryCode;
        this.createDate = createDate;
        this.count = count;
    }

    public String getId() {
        return id;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public float getCount() {
        return count;
    }

    public void setCount(float count) {
        this.count = count;
    }

    public Date getCreateDate() {
        return createDate;
    }

}
