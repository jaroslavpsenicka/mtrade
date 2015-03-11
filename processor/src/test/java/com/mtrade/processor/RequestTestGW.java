package com.mtrade.processor;

import com.mtrade.model.TradeRequest;

import java.util.List;
import java.util.Map;

/**
 * @author jaroslav.psenicka@gmail.com
 */
public interface RequestTestGW {

    void send(Map<String, Map<Integer, List<Object>>> requests);

}
