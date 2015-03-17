package com.mtrade.common.repository;

import com.mtrade.common.model.ExchangeStats;
import com.mtrade.common.model.StatsType;
import com.mtrade.common.model.ThroughputStats;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.math.BigInteger;
import java.util.List;

/**
 * @author jaroslav.psenicka@gmail.com
 */
public interface ExchangeStatsRepository extends MongoRepository<ExchangeStats, BigInteger> {

    @Cacheable("exchange-stats")
    @Query("{}")
    List<ExchangeStats> find(Pageable pageable);
}

