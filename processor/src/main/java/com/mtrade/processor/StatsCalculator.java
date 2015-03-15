//
// Copyright (c) 2011-2015 Xanadu Consultancy Ltd., 
//

package com.mtrade.processor;

import com.mtrade.common.model.Stats;
import com.mtrade.common.model.TradeRequest;
import com.mtrade.common.model.StatsType;
import com.mtrade.common.repository.StatsRepository;
import com.mtrade.processor.repository.StatsExecutionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;

import com.mtrade.processor.model.StatsExecution;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class StatsCalculator {

    @Autowired
    private StatsExecutionRepository executionRepository;

    @Autowired
    private StatsRepository repository;

    @Autowired
    private MongoTemplate mongoTemplate;

    private static final long MSINDAY = 1000 * 60 * 60 * 24;
    private static final Logger LOG = LoggerFactory.getLogger(StatsCalculator.class);

    public void calculateHourlyStats() {
        LOG.info("Calculating hourly stats");
        StatsExecution execution = readExecutionInfo(StatsType.HOUR);
        try {
            repository.save(getHourlyStats(execution));
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
        StatsExecution execution = readExecutionInfo(StatsType.DAY);
        try {
            repository.save(getDailyStats(execution));
            repository.save(getOverallStats(execution));
            execution.setLastSuccess(new Date());
        } catch (Exception ex) {
            LOG.error("Error calculating daily stats", ex);
            execution.setLastFailure(new Date());
        } finally {
            executionRepository.save(execution);
        }
    }

    private StatsExecution readExecutionInfo(StatsType type) {
        StatsExecution info = executionRepository.findByType(type);
        return (info != null) ? info : new StatsExecution(type);
    }

    private Iterable<Stats> getHourlyStats(StatsExecution execution) {
        Date createDate = new Date();
        AggregationOperation match = Aggregation.match(Criteria.where("timeCreated").gt(execution.getLastSuccess()));
        AggregationOperation group = Aggregation.group("originatingCountry").count().as("count");
        Aggregation agg = Aggregation.newAggregation(match, group);
        AggregationResults<Stats> results = this.mongoTemplate.aggregate(agg, TradeRequest.class, Stats.class);

        List<Stats> hourlyStats = new ArrayList<>();
        long period = (createDate.getTime() - execution.getLastSuccess().getTime()) / 3600000;
        for (Stats result : results.getMappedResults()) {
            Stats countryStats = new Stats(result.getId(), StatsType.HOUR);
            countryStats.setCreateDate(createDate);
            countryStats.setCount(result.getCount() / period);
            hourlyStats.add(countryStats);
        }

        return hourlyStats;
    }

    private Iterable<Stats> getDailyStats(StatsExecution execution) {
        Date createDate = new Date();
        Date midnight = new Date(System.currentTimeMillis() / MSINDAY * MSINDAY);
        AggregationOperation match = Aggregation.match(Criteria.where("createDate")
            .gt(execution.getLastSuccess()).lte(midnight).and("type").is(StatsType.HOUR));
        AggregationOperation group = Aggregation.group("countryCode").avg("count").as("count");
        Aggregation agg = Aggregation.newAggregation(match, group);
        AggregationResults<Stats> results = this.mongoTemplate.aggregate(agg, Stats.class, Stats.class);

        List<Stats> dailyStats = new ArrayList<>();
        for (Stats result : results.getMappedResults()) {
            Stats countryStats = new Stats(result.getId(), StatsType.DAY);
            countryStats.setCreateDate(createDate);
            countryStats.setCount(result.getCount());
            dailyStats.add(countryStats);
        }

        return dailyStats;
    }

    private Iterable<Stats> getOverallStats(StatsExecution execution) {
        Date createDate = new Date();
        AggregationOperation match = Aggregation.match(Criteria.where("type").is(StatsType.DAY));
        AggregationOperation group = Aggregation.group("countryCode").avg("count").as("count");
        Aggregation agg = Aggregation.newAggregation(match, group);
        AggregationResults<Stats> results = this.mongoTemplate.aggregate(agg, Stats.class, Stats.class);

        List<Stats> overallStats = new ArrayList<>();
        for (Stats result : results.getMappedResults()) {
            Stats countryStats = new Stats(result.getId(), StatsType.OVERALL);
            countryStats.setCreateDate(createDate);
            countryStats.setCount(result.getCount());
            overallStats.add(countryStats);
        }

        return overallStats;
    }

}
