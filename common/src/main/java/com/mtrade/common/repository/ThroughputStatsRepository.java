package com.mtrade.common.repository;

import com.mtrade.common.model.ThroughputStats;
import com.mtrade.common.model.StatsType;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.math.BigInteger;
import java.util.List;

/**
 * @author jaroslav.psenicka@gmail.com
 */
public interface ThroughputStatsRepository extends MongoRepository<ThroughputStats, BigInteger> {

    @Cacheable("throughput-stats")
    List<ThroughputStats> findByTypeOrderByCreateDateDesc(StatsType type);

    @Cacheable("throughput-stats")
    List<ThroughputStats> findFirst30ByTypeAndCountryCodeOrderByCreateDateDesc(StatsType type, String countryCode);

    @Cacheable("throughput-stats")
    List<ThroughputStats> findFirst30ByTypeOrderByCreateDateDesc(StatsType type);
}

