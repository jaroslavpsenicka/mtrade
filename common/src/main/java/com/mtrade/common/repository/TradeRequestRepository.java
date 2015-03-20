package com.mtrade.common.repository;

import com.mtrade.common.model.TradeRequest;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.CrudRepository;

import java.math.BigInteger;

/**
 * @author jaroslav.psenicka@gmail.com
 */
public interface TradeRequestRepository extends MongoRepository<TradeRequest, BigInteger> {

}

