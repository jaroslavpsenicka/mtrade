package com.mtrade.common.repository;

import com.mtrade.common.model.ThroughputStats;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * @author jaroslav.psenicka@gmail.com
 */
@Component
public class ThroughputStatsRepositoryImpl implements ThroughputStatsCustom {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public Map<String, Float> actualStats(String countryCode) {
        AggregationOperation sort = Aggregation.sort(Sort.Direction.DESC, "createDate");
        AggregationOperation group = Aggregation.group("countryCode").first("count").as("count");
        Aggregation agg = StringUtils.isEmpty(countryCode) ? Aggregation.newAggregation(sort, group)
            : Aggregation.newAggregation(Aggregation.match(Criteria.where("countryCode").is(countryCode)), sort, group);

        Map<String, Float> tputResults = new HashMap<>();
        for (ThroughputStats stats : mongoTemplate.aggregate(agg, ThroughputStats.class, ThroughputStats.class)) {
            tputResults.put(stats.getId(), stats.getCount());
        }

        return tputResults;
    }

    @Override
    public List<List<Number>> throughputStats(String countryCode) {
        AggregationOperation group = Aggregation.group("createDate").sum("count").as("count");
        AggregationOperation sort = Aggregation.sort(Sort.Direction.DESC, "_id");
        Aggregation agg = StringUtils.isEmpty(countryCode) ? Aggregation.newAggregation(group, sort)
            : Aggregation.newAggregation(Aggregation.match(Criteria.where("countryCode").is(countryCode)), group, sort);

        List<List<Number>> results = new ArrayList<>();
        for (IdCount stats : mongoTemplate.aggregate(agg, ThroughputStats.class, IdCount.class)) {
            results.add(0, Arrays.asList((Number) stats.getId().getTime(), stats.getCount()));
        }

        return results;
    }

    private static class IdCount {

        private Date id;
        private Float count;

        public Date getId() {
            return id;
        }
        public Float getCount() {
            return count;
        }
    }

}
