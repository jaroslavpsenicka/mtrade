package com.mtrade.processor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.mtrade.common.model.TradeRequest;
import com.mtrade.processor.repository.TradeRequestRepository;

import static org.junit.Assert.fail;

/**
 * @author jaroslav.psenicka@gmail.com
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:/processor-context.xml", "classpath:/test-context.xml"})
public class RequestWriterTest {

    @Autowired
    private RequestTestGW requestGW;

    @Autowired
    private TradeRequestRepository repository;

    private TradeRequest req1;
    private TradeRequest req2;

    private static final int RETRY = 20;

    @Before
    public void before() throws IOException {
        req1 = new TradeRequest();
        req1.setUserId("user");
        req1.setTransactionId("tx");
        req1.setAmountBuy((float) 1);
        req1.setAmountSell((float) 27);
        req1.setCurrencyFrom("CZK");
        req1.setCurrencyTo("EUR");
        req1.setOriginatingCountry("CZ");
        req2 = new TradeRequest();
        req2.setUserId("user");
        req2.setTransactionId("tx");
        req2.setAmountBuy((float) 2);
        req2.setAmountSell((float) 54);
        req2.setCurrencyFrom("CZK");
        req2.setCurrencyTo("EUR");
        req2.setOriginatingCountry("CZ");

        repository.deleteAll();
    }

    @After
    public void after() {
    }

    @Test
    public void validRequest() throws Exception {
        Map<Integer, List<Object>> partitionData = new HashMap<>();
        partitionData.put(0, new ArrayList<Object>(Arrays.asList(req1, req2)));
        Map<String, Map<Integer, List<Object>>> input = new HashMap<>();
        input.put("REQS", partitionData);
        requestGW.send(input);

        retryUntilEquals(2L, new Callable() {
            public Object call() throws Exception {
                return repository.count();
            }
        });
    }

    private void retryUntilEquals(Object value, Callable callable) throws Exception {
        for (int i = 0; i < RETRY; i++) {
            if (value.equals(callable.call())) {
                return;
            }
            Thread.sleep(100);
        }

        fail("value " + value + " not provided within " + (RETRY * 100) + "ms");
    }

}

