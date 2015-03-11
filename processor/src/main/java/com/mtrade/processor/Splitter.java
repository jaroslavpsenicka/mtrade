//
// Copyright (c) 2011-2014 Xanadu Consultancy Ltd., 
//

package com.mtrade.processor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.mtrade.model.TradeRequest;

public class Splitter {

    private static final Logger LOG = Logger.getLogger(Splitter.class);

    public List<TradeRequest> split(Map kafkaMessage) {
        ArrayList<TradeRequest> messages = new ArrayList<TradeRequest>();
        extract(kafkaMessage, messages);
        return messages;
    }

    private void extract(Object message, List<TradeRequest> messages) {
        if (message instanceof Map) {
            for (Object element : ((Map) message).values()) {
                extract(element, messages);
            }
        } else if (message instanceof List) {
            for (Object element : ((List) message)) {
                extract(element, messages);
            }
        } else if (message instanceof TradeRequest) {
            messages.add((TradeRequest) message);
        } else if (message != null) {
            throw new IllegalArgumentException("illegal object: "+message+" "+message.getClass());
        }
    }

}
