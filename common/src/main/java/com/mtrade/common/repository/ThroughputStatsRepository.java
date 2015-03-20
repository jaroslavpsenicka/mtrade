package com.mtrade.common.repository;

import com.mtrade.common.model.ThroughputStats;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.math.BigInteger;

/**
 * @author jaroslav.psenicka@gmail.com
 */
public interface ThroughputStatsRepository extends MongoRepository<ThroughputStats, BigInteger>, ThroughputStatsCustom {

}

