package com.mtrade.processor.repository;

import com.mtrade.processor.model.Stats;
import com.mtrade.processor.model.StatsExecution;
import com.mtrade.processor.model.StatsType;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.math.BigInteger;
import java.util.List;

/**
 * @author jaroslav.psenicka@gmail.com
 */
public interface StatsRepository extends MongoRepository<Stats, BigInteger> {

    List<Stats> findByType(StatsType type);

}

