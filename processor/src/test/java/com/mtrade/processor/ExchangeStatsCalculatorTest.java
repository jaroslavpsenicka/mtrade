package com.mtrade.processor;

import com.mtrade.common.model.ExchangeStats;
import com.mtrade.common.model.TradeRequest;
import com.mtrade.common.repository.ExchangeStatsRepository;
import com.mtrade.common.repository.TradeRequestRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author jaroslav.psenicka@gmail.com
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:/processor-context.xml", "classpath:/test-context.xml"})
public class ExchangeStatsCalculatorTest {

    @Autowired
    private StatsCalculator calculator;

    @Autowired
    private TradeRequestRepository tradeRequestRepository;

    @Autowired
    private ExchangeStatsRepository statsRepository;

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
    public void simpleExchangeStats() {
        calculator.calculateStats();
        Pageable page = new PageRequest(0, 5, Sort.Direction.DESC, "createDate");
        List<ExchangeStats> stats = statsRepository.find(page);
        assertEquals(1, stats.size());
        ExchangeStats exchangeStats = stats.get(0);
        assertEquals("CZK", exchangeStats.getCurrencyFrom());
        assertEquals("EUR", exchangeStats.getCurrencyTo());
        assertEquals("CZ", exchangeStats.getOriginatingCountry());
        assertEquals(new Integer(2), exchangeStats.getCount());
        assertEquals(81, exchangeStats.getAmount(), 0.1);
    }

    @Test
    public void bothSidesExchangeStats() {
        TradeRequest req3 = new TradeRequest();
        req3.setUserId("user");
        req3.setTransactionId("tx");
        req3.setAmountBuy((float) 27);
        req3.setAmountSell((float) 1);
        req3.setCurrencyFrom("EUR");
        req3.setCurrencyTo("CZK");
        req3.setOriginatingCountry("CZ");
        req3.setTimeCreated(new Date());
        req3.setTimePlaced(new Date());
        tradeRequestRepository.save(req3);

        calculator.calculateStats();
        Pageable page = new PageRequest(0, 5, Sort.Direction.DESC, "createDate");
        List<ExchangeStats> stats = statsRepository.find(page);
        assertEquals(2, stats.size());
        ExchangeStats rec = stats.get(0);
        assertEquals("CZK", rec.getCurrencyFrom());
        assertEquals("EUR", rec.getCurrencyTo());
        assertEquals("CZ", rec.getOriginatingCountry());
        assertEquals(new Integer(2), rec.getCount());
        assertEquals(81, rec.getAmount(), 0.1);
        rec = stats.get(1);
        assertEquals("EUR", rec.getCurrencyFrom());
        assertEquals("CZK", rec.getCurrencyTo());
        assertEquals("CZ", rec.getOriginatingCountry());
        assertEquals(new Integer(1), rec.getCount());
        assertEquals(1, rec.getAmount(), 0.1);
    }

}
