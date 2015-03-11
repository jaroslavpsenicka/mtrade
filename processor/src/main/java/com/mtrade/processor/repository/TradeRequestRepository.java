package com.mtrade.processor.repository;

import com.mtrade.model.TradeRequest;
import org.springframework.data.repository.CrudRepository;

import java.math.BigInteger;

/**
 * @author jaroslav.psenicka@gmail.com
 */
public interface TradeRequestRepository extends CrudRepository<TradeRequest, BigInteger> {

}

