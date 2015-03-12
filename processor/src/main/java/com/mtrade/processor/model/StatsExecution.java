//
// Copyright (c) 2011-2015 Xanadu Consultancy Ltd., 
//

package com.mtrade.processor.model;

import com.mtrade.processor.StatsCalculator;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document
public class StatsExecution {

    @Id
    private String id;
    private String type;
    private Date lastSuccess;
    private Date lastFailure;

    public StatsExecution() {
        this.lastSuccess = new Date(0);
        this.lastFailure = new Date(0);
    }

    public StatsExecution(String type) {
        this();
        this.type = type;
    }

    public StatsExecution(String type, Date lastSuccessfulExecution) {
        this();
        this.type = type;
        this.lastSuccess = lastSuccessfulExecution;
    }


    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Date getLastSuccess() {
        return lastSuccess;
    }

    public void setLastSuccess(Date lastSuccess) {
        this.lastSuccess = lastSuccess;
    }

    public Date getLastFailure() {
        return lastFailure;
    }

    public void setLastFailure(Date lastFailure) {
        this.lastFailure = lastFailure;
    }
}
