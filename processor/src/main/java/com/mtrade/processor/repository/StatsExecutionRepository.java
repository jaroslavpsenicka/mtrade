package com.mtrade.processor.repository;

import com.mtrade.processor.model.HourlyStats;
import com.mtrade.processor.model.StatsExecution;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.math.BigInteger;

/**
 * @author jaroslav.psenicka@gmail.com
 */
public interface StatsExecutionRepository extends MongoRepository<StatsExecution, BigInteger> {

    StatsExecution findByType(String type);
}

