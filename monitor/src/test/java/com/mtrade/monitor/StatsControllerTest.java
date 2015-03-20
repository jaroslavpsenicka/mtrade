package com.mtrade.monitor;

import com.mtrade.common.model.ExchangeStats;
import com.mtrade.common.model.ThroughputStats;
import com.mtrade.common.repository.ExchangeStatsRepository;
import com.mtrade.common.repository.ThroughputStatsRepository;
import com.mtrade.monitor.model.User;
import com.mtrade.monitor.repository.UserRepository;
import net.sf.ehcache.Ehcache;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.util.NestedServletException;

import java.util.ArrayList;
import java.util.Date;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * @author jaroslav.psenicka@gmail.com
 */
@WebAppConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:/monitor-context.xml", "classpath:/security-context.xml", "classpath:/test-context.xml"})
public class StatsControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ThroughputStatsRepository tputStatsRepository;

    @Autowired
    private ExchangeStatsRepository xchgStatsRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CacheManager cacheManager;

    private MockMvc mockMvc;

    private long timeConstant = 1000;

    @Before
    public void before() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        userRepository.save(new User("usr1"));
        tputStatsRepository.save(new ThroughputStats("CZ", new Date(1), new Float(1.0)));
        tputStatsRepository.save(new ThroughputStats("DE", new Date(1), new Float(2.0)));
        tputStatsRepository.save(new ThroughputStats("DE", new Date(10000), new Float(3.0)));
        xchgStatsRepository.save(createExchangeStats("CZK", "EUR", 12));
    }

    @After
    public void after() {
        userRepository.deleteAll();
        tputStatsRepository.deleteAll();
        xchgStatsRepository.deleteAll();
        SecurityContextHolder.getContext().setAuthentication(null);
        Ehcache cache = (Ehcache) cacheManager.getCache("stats").getNativeCache();
        cache.removeAll();
    }

    @Test
    public void actualStats() throws Exception {
        authenticate("usr1", "ADMIN");
        mockMvc.perform(get("/stats"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("tput.CZ", is(1.0)))
            .andExpect(jsonPath("tput.DE", is(3.0)))
            .andExpect(jsonPath("xchg", hasSize(1)))
            .andExpect(jsonPath("xchg[0].currencyFrom", is("CZK")))
            .andExpect(jsonPath("xchg[0].currencyTo", is("EUR")))
            .andExpect(jsonPath("xchg[0].amount", is(12.0)))
            .andExpect(jsonPath("xchg[0].count", is(1)));
    }

    @Test
    public void throughputStats() throws Exception {
        authenticate("usr1", "ADMIN");
        mockMvc.perform(get("/stats/tput"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[0][1]", is(3.0)))
            .andExpect(jsonPath("$[1][1]", is(3.0)));
    }

    @Test
    public void czActualStats() throws Exception {
        authenticate("usr1", "ADMIN");
        mockMvc.perform(get("/stats?cc=CZ"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("tput.CZ", is(1.0)));
    }

    @Test
    public void czThroughputStats() throws Exception {
        authenticate("usr1", "ADMIN");
        mockMvc.perform(get("/stats/tput?cc=CZ"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0][1]", is(1.0)));
    }

    @Test
    public void noUser() throws Exception {
        try {
            mockMvc.perform(get("/stats"));
            fail("should not allow");
        } catch (NestedServletException ex) {
            assertTrue(ex.getCause() instanceof AuthenticationException);
        }
    }

    @Test
    public void illegalUser() throws Exception {
        Authentication authentication = new UsernamePasswordAuthenticationToken("illegal", "pwd");
        SecurityContextHolder.getContext().setAuthentication(authentication);
        try {
            mockMvc.perform(get("/stats"));
            fail("should not allow");
        } catch (NestedServletException ex) {
            assertTrue(ex.getCause() instanceof AuthenticationException);
        }
    }

    @Test
    public void notInRole() throws Exception {
        authenticate("usr1", "USER");
        try {
            mockMvc.perform(get("/stats"));
            fail("should not allow");
        } catch (NestedServletException ex) {
            assertTrue(ex.getCause() instanceof AccessDeniedException);
        }
    }

    private void authenticate(String user, String role) {
        ArrayList<SimpleGrantedAuthority> roles = new ArrayList<>();
        roles.add(new SimpleGrantedAuthority(role));
        Authentication authentication = new UsernamePasswordAuthenticationToken(user, "pwd", roles);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private ExchangeStats createExchangeStats(String from, String to, float amount) {
        ExchangeStats stats = new ExchangeStats();
        stats.setCreateDate(new Date(timeConstant++));
        stats.setCurrencyFrom(from);
        stats.setCurrencyTo(to);
        stats.setAmount(amount);
        stats.setCount(1);
        return stats;
    }

}

