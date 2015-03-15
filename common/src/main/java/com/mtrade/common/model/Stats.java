package com.mtrade.common.model;

import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * @author jaroslav.psenicka@gmail.com
 */
public class Stats {

    private String id;

    @NotNull
    private String countryCode;

    @NotNull
    private StatsType type;

    @NotNull
    private Date createDate;

    private float count;

    public Stats() {
    }

    public Stats(String countryCode, StatsType type) {
        this.countryCode = countryCode;
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public StatsType getType() {
        return type;
    }

    public void setType(StatsType type) {
        this.type = type;
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

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }
}
