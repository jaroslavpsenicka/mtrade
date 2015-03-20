package com.mtrade.common.repository;

import com.mtrade.common.model.ExchangeStats;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.math.BigInteger;
import java.util.List;

/**
 * @author jaroslav.psenicka@gmail.com
 */
public interface ExchangeStatsRepository extends MongoRepository<ExchangeStats, BigInteger> {

    @Query("{}")
    @Cacheable("exchange-stats")
    List<ExchangeStats> find(Pageable pageable);
}

