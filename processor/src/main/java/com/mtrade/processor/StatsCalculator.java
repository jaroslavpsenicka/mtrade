//
// Copyright (c) 2011-2015 Xanadu Consultancy Ltd., 
//

package com.mtrade.processor;

import com.mtrade.common.model.ExchangeStats;
import com.mtrade.common.model.ThroughputStats;
import com.mtrade.common.model.TradeRequest;
import com.mtrade.common.model.StatsType;
import com.mtrade.common.repository.ExchangeStatsRepository;
import com.mtrade.common.repository.ThroughputStatsRepository;
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

import com.mtrade.processor.model.StatsExecution;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class StatsCalculator {

    @Autowired
    private StatsExecutionRepository executionRepository;

    @Autowired
    private ThroughputStatsRepository throughputStatsRepository;

    @Autowired
    private ExchangeStatsRepository exchangeStatsRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    private static final long MSINDAY = 1000 * 60 * 60 * 24;
    private static final Logger LOG = LoggerFactory.getLogger(StatsCalculator.class);

    public void calculateHourlyStats() {
        LOG.info("Calculating hourly stats");
        StatsExecution execution = readExecutionInfo(StatsType.HOUR);
        try {
            throughputStatsRepository.save(getHourlyThroughputStats(execution));
            exchangeStatsRepository.save(getExchangeStats(execution));
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
            throughputStatsRepository.save(getDailyThroughputStats(execution));
            throughputStatsRepository.save(getOverallStats(execution));
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

    private Iterable<ThroughputStats> getHourlyThroughputStats(StatsExecution execution) {
        Date createDate = new Date();
        AggregationOperation match = Aggregation.match(Criteria.where("timeCreated").gt(execution.getLastSuccess()));
        AggregationOperation group = Aggregation.group("originatingCountry").count().as("count");
        Aggregation agg = Aggregation.newAggregation(match, group);
        AggregationResults<ThroughputStats> results = this.mongoTemplate.aggregate(agg,
            TradeRequest.class, ThroughputStats.class);

        List<ThroughputStats> hourlyStats = new ArrayList<>();
        long period = (createDate.getTime() - execution.getLastSuccess().getTime()) / 3600000;
        for (ThroughputStats result : results.getMappedResults()) {
            ThroughputStats countryStats = new ThroughputStats(result.getId(), StatsType.HOUR);
            countryStats.setCreateDate(createDate);
            countryStats.setCount(result.getCount() / period);
            hourlyStats.add(countryStats);
        }

        return hourlyStats;
    }

    private Iterable<ThroughputStats> getDailyThroughputStats(StatsExecution execution) {
        Date createDate = new Date();
        Date midnight = new Date(System.currentTimeMillis() / MSINDAY * MSINDAY);
        AggregationOperation match = Aggregation.match(Criteria.where("createDate")
            .gt(execution.getLastSuccess()).lte(midnight).and("type").is(StatsType.HOUR));
        AggregationOperation group = Aggregation.group("countryCode").avg("count").as("count");
        Aggregation agg = Aggregation.newAggregation(match, group);
        AggregationResults<ThroughputStats> results = this.mongoTemplate.aggregate(agg,
            ThroughputStats.class, ThroughputStats.class);

        List<ThroughputStats> dailyStats = new ArrayList<>();
        for (ThroughputStats result : results.getMappedResults()) {
            ThroughputStats countryStats = new ThroughputStats(result.getId(), StatsType.DAY);
            countryStats.setCreateDate(createDate);
            countryStats.setCount(result.getCount());
            dailyStats.add(countryStats);
        }

        return dailyStats;
    }

    private Iterable<ThroughputStats> getOverallStats(StatsExecution execution) {
        Date createDate = new Date();
        AggregationOperation match = Aggregation.match(Criteria.where("type").is(StatsType.DAY));
        AggregationOperation group = Aggregation.group("countryCode").avg("count").as("count");
        Aggregation agg = Aggregation.newAggregation(match, group);
        AggregationResults<ThroughputStats> results = this.mongoTemplate.aggregate(agg,
            ThroughputStats.class, ThroughputStats.class);

        List<ThroughputStats> overallStats = new ArrayList<>();
        for (ThroughputStats result : results.getMappedResults()) {
            ThroughputStats countryStats = new ThroughputStats(result.getId(), StatsType.OVERALL);
            countryStats.setCreateDate(createDate);
            countryStats.setCount(result.getCount());
            overallStats.add(countryStats);
        }

        return overallStats;
    }

    private Iterable<ExchangeStats> getExchangeStats(StatsExecution execution) {
        Date createDate = new Date();
        AggregationOperation group = Aggregation.group("currencyFrom", "currencyTo").count().as("count")
            .sum("amountSell").as("amount");
        AggregationOperation sort = Aggregation.sort(Sort.Direction.DESC, "count");
        AggregationOperation limit = Aggregation.limit(5);
        Aggregation agg = Aggregation.newAggregation(group, sort, limit);
        AggregationResults<ExchangeStats> results = this.mongoTemplate.aggregate(agg,
            TradeRequest.class, ExchangeStats.class);

        List<ExchangeStats> exchangeStats = new ArrayList<>();
        for (ExchangeStats result : results.getMappedResults()) {
            result.setCreateDate(createDate);
            exchangeStats.add(result);
        }

        return exchangeStats;
    }

}
