package com.mtrade.processor;

import com.mtrade.processor.model.HourlyStats;
import com.mtrade.common.model.TradeRequest;
import com.mtrade.processor.model.StatsExecution;
import com.mtrade.processor.repository.HourlyStatsRepository;
import com.mtrade.processor.repository.StatsExecutionRepository;
import com.mtrade.processor.repository.TradeRequestRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.util.*;

import static org.junit.Assert.assertEquals;

/**
 * @author jaroslav.psenicka@gmail.com
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:/processor-context.xml", "classpath:/test-context.xml"})
public class StatsCalculatorTest {

    @Autowired
    private StatsCalculator calculator;

    @Autowired
    private TradeRequestRepository tradeRequestRepository;

    @Autowired
    private HourlyStatsRepository hourlyStatsRepository;

    @Autowired
    private StatsExecutionRepository executionRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Before
    public void before() throws IOException {
        TradeRequest req1 = new TradeRequest();
        req1.setUserId("user");
        req1.setTransactionId("tx");
        req1.setAmountBuy((float) 1);
        req1.setAmountSell((float) 27);
        req1.setCurrencyFrom("CZK");
        req1.setCurrencyTo("EUR");
        req1.setOriginatingCountry("CZ");
        req1.setTimeCreated(new Date(1));
        TradeRequest req2 = new TradeRequest();
        req2.setUserId("user");
        req2.setTransactionId("tx");
        req2.setAmountBuy((float) 2);
        req2.setAmountSell((float) 54);
        req2.setCurrencyFrom("CZK");
        req2.setCurrencyTo("EUR");
        req2.setOriginatingCountry("CZ");
        req2.setTimeCreated(new Date(1));
        tradeRequestRepository.deleteAll();
        tradeRequestRepository.save(Arrays.asList(req1, req2));
        hourlyStatsRepository.deleteAll();
    }

    @After
    public void after() {
    }

    @Test
    public void hourlyStats() throws Exception {
        calculator.calculateHourlyStats();
        assertEquals(1, hourlyStatsRepository.count());
        HourlyStats hourlyStats = hourlyStatsRepository.findAll().iterator().next();
        assertEquals("CZ", hourlyStats.getCountryCode());
        assertEquals(2, hourlyStats.getCount());
    }

    @Test
    public void hourlyStatsExistingInfo() throws Exception {
        executionRepository.save(new StatsExecution(StatsCalculator.HOUR, new Date()));
        calculator.calculateHourlyStats();
        assertEquals(0, hourlyStatsRepository.count());
    }
}
