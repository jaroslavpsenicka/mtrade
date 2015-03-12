//
// Copyright (c) 2011-2015 Xanadu Consultancy Ltd., 
//

package com.mtrade.processor.model;

import java.util.Date;

public class StatsExecutionInfo {

    private Date lastSuccess;
    private Date lastFailure;

    public StatsExecutionInfo() {
        this.lastSuccess = new Date(0);
        this.lastFailure = new Date(0);
    }

    public StatsExecutionInfo(Date lastSuccessfulExecution) {
        this.lastSuccess = lastSuccessfulExecution;
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
