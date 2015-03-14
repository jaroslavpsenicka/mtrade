package com.mtrade.processor.model;

import com.mtrade.processor.StatsCalculator;
import oracle.jrockit.jfr.openmbean.JFRStatsType;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

/**
 * @author jaroslav.psenicka@gmail.com
 */
@Document
public class Stats {

    @Id
    private String id;

    private String countryCode;
    private StatsType type;
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

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
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
