//
// Copyright (c) 2011-2015 Xanadu Consultancy Ltd., 
//

package com.mtrade.processor;

import com.mtrade.processor.model.HourlyStats;
import com.mtrade.common.model.TradeRequest;
import com.mtrade.processor.repository.HourlyStatsRepository;
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

import com.mtrade.processor.model.StatsExecutionInfo;

import java.util.Date;

public class StatsCalculator {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private HourlyStatsRepository repository;

    public static final String HOURLY_STATS = "hourlyStats";
    private static final Logger LOG = LoggerFactory.getLogger(StatsCalculator.class);

    public void calculateHourlyStats() {
        LOG.info("Calculating hourly stats");
        StatsExecutionInfo executionInfo = readExecutionInfo(HOURLY_STATS);
        try {
            AggregationResults<HourlyStats> result = this.mongoTemplate.aggregate(getHourlyAggregation(executionInfo),
                TradeRequest.class, HourlyStats.class);
            repository.save(result.getMappedResults());
            executionInfo.setLastSuccess(new Date());
        } catch (Exception ex) {
            LOG.error("Error calculating hourly stats", ex);
            executionInfo.setLastFailure(new Date());
        } finally {
            mongoTemplate.save(executionInfo, HOURLY_STATS);
        }
    }

    public void calculateDailyStats() {
        LOG.info("Calculating daily stats");

    }

    private StatsExecutionInfo readExecutionInfo(String collection) {
        Query query = new Query().with(new Sort(Sort.Direction.DESC, "lastSuccesDate")).limit(1);
        StatsExecutionInfo info = mongoTemplate.findOne(query, StatsExecutionInfo.class, collection);
        return (info != null) ? info : new StatsExecutionInfo();
    }

    private Aggregation getHourlyAggregation(StatsExecutionInfo info) {
        AggregationOperation match = Aggregation.match(Criteria.where("timeCreated").gte(info.getLastSuccess()));
        AggregationOperation group = Aggregation.group("originatingCountry").count().as("count");
        return Aggregation.newAggregation(match, group);
    }

}
