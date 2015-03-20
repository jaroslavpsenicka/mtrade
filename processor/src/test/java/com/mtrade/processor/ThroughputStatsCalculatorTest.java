package com.mtrade.processor;

import com.mtrade.common.model.ThroughputStats;
import com.mtrade.common.model.TradeRequest;
import com.mtrade.common.repository.ThroughputStatsRepository;
import com.mtrade.common.repository.TradeRequestRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;

import static org.junit.Assert.assertEquals;

/**
 * @author jaroslav.psenicka@gmail.com
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:/processor-context.xml", "classpath:/test-context.xml"})
public class ThroughputStatsCalculatorTest {

    @Autowired
    private StatsCalculator calculator;

    @Autowired
    private TradeRequestRepository tradeRequestRepository;

    @Autowired
    private ThroughputStatsRepository statsRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    private static final long HOURMS = 60 * 60 * 1000;

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
        req1.setTimeCreated(new Date());
        req1.setTimePlaced(new Date());
        TradeRequest req2 = new TradeRequest();
        req2.setUserId("user");
        req2.setTransactionId("tx");
        req2.setAmountBuy((float) 2);
        req2.setAmountSell((float) 54);
        req2.setCurrencyFrom("CZK");
        req2.setCurrencyTo("EUR");
        req2.setOriginatingCountry("CZ");
        req2.setTimeCreated(new Date());
        req2.setTimePlaced(new Date());
        tradeRequestRepository.deleteAll();
        tradeRequestRepository.save(Arrays.asList(req1, req2));
        statsRepository.deleteAll();
    }

    @After
    public void after() {
    }

    @Test
    public void hourlyStats() throws Exception {
        calculator.calculateStats();
        assertEquals(1, statsRepository.count());
        ThroughputStats stats = statsRepository.findAll().iterator().next();
        assertEquals("CZ", stats.getCountryCode());
        assertEquals(5E-6, stats.getCount(), 1E-6);
    }

}
