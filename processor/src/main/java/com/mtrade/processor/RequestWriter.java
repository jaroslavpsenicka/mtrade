//
// Copyright (c) 2011-2015 Xanadu Consultancy Ltd., 
//

package com.mtrade.processor;

import java.util.List;
import java.util.Map;

import com.mtrade.model.TradeRequest;
import com.mtrade.processor.repository.TradeRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;

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
