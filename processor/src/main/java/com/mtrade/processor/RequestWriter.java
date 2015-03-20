//
// Copyright (c) 2011-2015 Xanadu Consultancy Ltd., 
//

package com.mtrade.processor;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.mtrade.common.model.TradeRequest;
import com.mtrade.common.repository.TradeRequestRepository;

public class RequestWriter {

    @Autowired
    private TradeRequestRepository repository;

    public void write(Map<String, Map<Integer, List<TradeRequest>>> requests) {
        for (Map<Integer, List<TradeRequest>> map : requests.values()) {
            for (List<TradeRequest> objects : map.values()) {
                repository.save(objects);
            }
        }
    }

}
