package com.mtrade.processor;

import com.mtrade.common.model.TradeRequest;
import com.mtrade.processor.model.Stats;
import com.mtrade.processor.model.StatsExecution;
import com.mtrade.processor.model.StatsType;
import com.mtrade.processor.repository.StatsRepository;
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
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

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
    private StatsRepository statsRepository;

    @Autowired
    private StatsExecutionRepository executionRepository;

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
        statsRepository.deleteAll();
        executionRepository.deleteAll();
    }

    @After
    public void after() {
    }

    @Test
    public void hourlyStats() throws Exception {
        calculator.calculateHourlyStats();
        assertEquals(1, statsRepository.count());
        Stats stats = statsRepository.findAll().iterator().next();
        assertEquals("CZ", stats.getCountryCode());
        assertEquals(StatsType.HOUR, stats.getType());
        assertEquals(5E-6, stats.getCount(), 1E-6);
    }

    @Test
    public void hourlyStatsExistingInfo() throws Exception {
        executionRepository.save(new StatsExecution(StatsType.HOUR, new Date()));
        calculator.calculateHourlyStats();
        assertEquals(0, statsRepository.count());
    }

    @Test
    public void dailyStats() {
        statsRepository.save(createDailyStats("CZ", 0, 1));
        statsRepository.save(createDailyStats("CZ", 1, 2));
        statsRepository.save(createDailyStats("CZ", 2, 3));
        calculator.calculateDailyStats();
        List<Stats> stats = statsRepository.findByType(StatsType.DAY);
        assertEquals(1, stats.size());
        assertEquals("CZ", stats.get(0).getCountryCode());
        assertEquals(StatsType.DAY, stats.get(0).getType());
        assertEquals(2, stats.get(0).getCount(), 0.1);
        assertNotNull(stats.get(0).getCreateDate());
    }

    @Test
    public void dailyStatsExistingInfo() {
        statsRepository.save(createDailyStats("CZ", 0, 1));
        statsRepository.save(createDailyStats("CZ", 1, 2));
        statsRepository.save(createDailyStats("CZ", 2, 3));
        Date date = new Date();
        executionRepository.save(new StatsExecution(StatsType.DAY, date));
        calculator.calculateDailyStats();
        List<Stats> statsList = statsRepository.findByType(StatsType.DAY);
        assertEquals(0, statsList.size());
    }

    @Test
    public void dailyStatsTwoCountries() {
        statsRepository.save(createDailyStats("CZ", 0, 1));
        statsRepository.save(createDailyStats("DE", 0, 2));
        statsRepository.save(createDailyStats("DE", 2, 4));
        calculator.calculateDailyStats();
        List<Stats> stats = statsRepository.findByType(StatsType.DAY);
        assertEquals(2, stats.size());
        assertEquals("DE", stats.get(0).getCountryCode());
        assertEquals(StatsType.DAY, stats.get(0).getType());
        assertEquals(3, stats.get(0).getCount(), 1);
        assertNotNull(stats.get(0).getCreateDate());
        assertEquals("CZ", stats.get(1).getCountryCode());
        assertEquals(StatsType.DAY, stats.get(1).getType());
        assertEquals(1, stats.get(1).getCount(), 0.1);
        assertNotNull(stats.get(1).getCreateDate());
    }

    private Stats createDailyStats(String country, int hourDiff, int count) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        cal.add(Calendar.HOUR, hourDiff);
        Stats stats = new Stats();
        stats.setType(StatsType.HOUR);
        stats.setCountryCode(country);
        stats.setCreateDate(cal.getTime());
        stats.setCount(count);
        return stats;
    }
}
