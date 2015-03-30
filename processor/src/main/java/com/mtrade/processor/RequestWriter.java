package com.mtrade.processor;

import com.mtrade.common.model.TradeRequest;
import com.mtrade.common.repository.TradeRequestRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

public class RequestWriter {

    @Autowired
    private TradeRequestRepository repository;

    private static final Logger LOG = LoggerFactory.getLogger(RequestWriter.class);

    public void write(Map<String, Map<Integer, List<TradeRequest>>> requests) {
        for (Map<Integer, List<TradeRequest>> map : requests.values()) {
            for (List<TradeRequest> objects : map.values()) {
                long diff = write(objects);
                LOG.info(Thread.currentThread().getName() + ": " + objects.size() + " objects saved in " + diff + "ms");
            }
        }
    }

    private long write(List<TradeRequest> objects) {
        long startTime = System.currentTimeMillis();
        repository.save(objects);
        return System.currentTimeMillis() - startTime;
    }

}
