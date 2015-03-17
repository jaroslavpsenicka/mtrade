package com.mtrade.common.repository;

import com.mtrade.common.model.Stats;
import com.mtrade.common.model.StatsType;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.math.BigInteger;
import java.util.List;

/**
 * @author jaroslav.psenicka@gmail.com
 */
public interface StatsRepository extends MongoRepository<Stats, BigInteger> {

    @Cacheable("stats")
    List<Stats> findByTypeOrderByCreateDateDesc(StatsType type);

    @Cacheable("stats")
    List<Stats> findFirst30ByTypeAndCountryCodeOrderByCreateDateDesc(StatsType type, String countryCode);

    @Cacheable("stats")
    List<Stats> findFirst30ByTypeOrderByCreateDateDesc(StatsType type);
}

