//
// Copyright (c) 2011-2015 Xanadu Consultancy Ltd., 
//

package com.mtrade.processor.model;

import com.mtrade.common.model.StatsType;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;
import java.util.Date;

@Document
public class StatsExecution {

    @Id
    private String id;

    @NotNull
    private StatsType type;

    @NotNull
    private Date lastSuccess;

    @NotNull
    private Date lastFailure;

    private StatsExecution() {
        this.lastSuccess = new Date(0);
        this.lastFailure = new Date(0);
    }

    public StatsExecution(StatsType type) {
        this();
        this.type = type;
    }

    public StatsExecution(StatsType type, Date lastSuccessfulExecution) {
        this();
        this.type = type;
        this.lastSuccess = lastSuccessfulExecution;
    }

    public StatsType getType() {
        return type;
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
