//
// Copyright (c) 2011-2015 Xanadu Consultancy Ltd., 
//

package com.mtrade.processor;

import com.mtrade.common.model.ExchangeStats;
import com.mtrade.common.model.ThroughputStats;
import com.mtrade.common.model.TradeRequest;
import com.mtrade.common.repository.ExchangeStatsRepository;
import com.mtrade.common.repository.ThroughputStatsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class StatsCalculator {

    @Autowired
    private ThroughputStatsRepository throughputStatsRepository;

    @Autowired
    private ExchangeStatsRepository exchangeStatsRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    private long period = 60000L;
    private boolean enabled = true;

    private static final Logger LOG = LoggerFactory.getLogger(StatsCalculator.class);

    public void setPeriod(long period) {
        this.period = period;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void calculateStats() {
        if (enabled) {
            LOG.info("Calculating stats");
            Date createDate = new Date();
            try {
                throughputStatsRepository.save(getThroughputStats(createDate));
                exchangeStatsRepository.save(getExchangeStats(createDate));
            } catch (Exception ex) {
                LOG.error("Error calculating stats", ex);
            }
        }
    }

    private Iterable<ThroughputStats> getThroughputStats(Date createDate) {
        Date lastSuccessDate = readLastSuccessDate();
        AggregationOperation match = Aggregation.match(Criteria.where("timeCreated").gt(lastSuccessDate));
        AggregationOperation group = Aggregation.group("originatingCountry").count().as("count");
        Aggregation agg = Aggregation.newAggregation(match, group);
        AggregationResults<ThroughputStats> results = this.mongoTemplate.aggregate(agg,
            TradeRequest.class, ThroughputStats.class);

        List<ThroughputStats> stats = new ArrayList<>();
        float relativePeriod = (createDate.getTime() - lastSuccessDate.getTime()) / this.period;
        for (ThroughputStats result : results.getMappedResults()) {
            ThroughputStats countryStats = new ThroughputStats(result.getId(), createDate);
            countryStats.setCount(result.getCount() / (relativePeriod > 0 ? relativePeriod : 1));
            stats.add(countryStats);
        }

        return stats;
    }

    private Date readLastSuccessDate() {
        PageRequest pageRequest = new PageRequest(0, 1, Sort.Direction.DESC, "createDate");
        List<ThroughputStats> stats = throughputStatsRepository.findAll(pageRequest).getContent();
        return (stats.size() > 0) ? stats.get(0).getCreateDate() : new Date(0);
    }

    private Iterable<ExchangeStats> getExchangeStats(Date createDate) {
        AggregationOperation group = Aggregation.group("originatingCountry", "currencyFrom", "currencyTo")
            .count().as("count").sum("amountSell").as("amount");
        AggregationOperation sort = Aggregation.sort(Sort.Direction.DESC, "count");
        Aggregation agg = Aggregation.newAggregation(group, sort);
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
