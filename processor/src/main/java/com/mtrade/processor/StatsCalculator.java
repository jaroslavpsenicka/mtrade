//
// Copyright (c) 2011-2015 Xanadu Consultancy Ltd., 
//

package com.mtrade.processor;

import com.mtrade.processor.model.HourlyStats;
import com.mtrade.common.model.TradeRequest;
import com.mtrade.processor.repository.HourlyStatsRepository;
import com.mtrade.processor.repository.StatsExecutionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import com.mtrade.processor.model.StatsExecution;

import java.util.Date;

public class StatsCalculator {

    @Autowired
    private StatsExecutionRepository executionRepository;

    @Autowired
    private HourlyStatsRepository repository;

    @Autowired
    private MongoTemplate mongoTemplate;

    public static final String HOUR = "HOUR";
    private static final Logger LOG = LoggerFactory.getLogger(StatsCalculator.class);

    public void calculateHourlyStats() {
        LOG.info("Calculating hourly stats");
        StatsExecution execution = readExecutionInfo(HOUR);
        try {
            AggregationResults<HourlyStats> result = this.mongoTemplate.aggregate(getHourlyAggregation(execution),
                TradeRequest.class, HourlyStats.class);
            repository.save(result.getMappedResults());
            execution.setLastSuccess(new Date());
        } catch (Exception ex) {
            LOG.error("Error calculating hourly stats", ex);
            execution.setLastFailure(new Date());
        } finally {
            executionRepository.save(execution);
        }
    }

    public void calculateDailyStats() {
        LOG.info("Calculating daily stats");

    }

    private StatsExecution readExecutionInfo(String type) {
        StatsExecution info = executionRepository.findByType(type);
        return (info != null) ? info : new StatsExecution(type);
    }

    private Aggregation getHourlyAggregation(StatsExecution info) {
        AggregationOperation match = Aggregation.match(Criteria.where("timeCreated").gt(info.getLastSuccess()));
        AggregationOperation group = Aggregation.group("originatingCountry").count().as("count");
        return Aggregation.newAggregation(match, group);
    }

}
