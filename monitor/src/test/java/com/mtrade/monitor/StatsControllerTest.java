package com.mtrade.monitor;

import com.mtrade.common.model.Stats;
import com.mtrade.common.model.StatsType;
import com.mtrade.common.repository.StatsRepository;
import com.mtrade.monitor.model.User;
import com.mtrade.monitor.repository.UserRepository;
import net.sf.ehcache.Ehcache;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
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
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;
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
    private StatsRepository statsRepository;

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
        statsRepository.save(createStats(StatsType.DAY, "CZ", 1));
        statsRepository.save(createStats(StatsType.DAY, "DE", 2));
        statsRepository.save(createStats(StatsType.DAY, "DE", 3));
        statsRepository.save(createStats(StatsType.DAY, "DE", 4));
        statsRepository.save(createStats(StatsType.OVERALL, "CZ", 1));
        statsRepository.save(createStats(StatsType.OVERALL, "DE", 3));
    }

    @After
    public void after() {
        statsRepository.deleteAll();
        userRepository.deleteAll();
        SecurityContextHolder.getContext().setAuthentication(null);
        Ehcache cache = (Ehcache) cacheManager.getCache("stats").getNativeCache();
        cache.removeAll();
    }

    @Test
    public void overallStats() throws Exception {
        authenticate("usr1", "ADMIN");
        mockMvc.perform(get("/stats"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("CZ", is(1.0)))
            .andExpect(jsonPath("DE", is(3.0)));
    }

    @Test
    public void czStats() throws Exception {
        authenticate("usr1", "ADMIN");
        mockMvc.perform(get("/stats/CZ"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0][1]", is(1.0)));
    }

    @Test
    public void deStats() throws Exception {
        authenticate("usr1", "ADMIN");
        mockMvc.perform(get("/stats/DE"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$", hasSize(3)))
            .andExpect(jsonPath("$[0][1]", is(2.0)))
            .andExpect(jsonPath("$[1][1]", is(3.0)))
            .andExpect(jsonPath("$[2][1]", is(4.0)));
    }

    @Test
    public void overallCache() {
        assertNotNull(statsRepository.findByTypeOrderByCreateDateDesc(StatsType.OVERALL));
        Ehcache cache = (Ehcache) cacheManager.getCache("stats").getNativeCache();
        List keys = cache.getKeys();
        assertEquals(1, keys.size());
        assertEquals(StatsType.OVERALL, keys.get(0));
    }

    @Test
    public void countryCache() {
        assertNotNull(statsRepository.findFirst30ByTypeAndCountryCodeOrderByCreateDateDesc(StatsType.DAY, "CZ"));
        Ehcache cache = (Ehcache) cacheManager.getCache("stats").getNativeCache();
        List keys = cache.getKeys();
        assertEquals(1, keys.size());
        assertTrue(keys.get(0).toString().contains(StatsType.DAY.name()));
        assertTrue(keys.get(0).toString().contains("CZ"));
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

    private Stats createStats(StatsType type, String country, int count) {
        Stats stats = new Stats(country, StatsType.DAY);
        stats.setCreateDate(new Date(timeConstant++));
        stats.setType(type);
        stats.setCount(count);
        return stats;
    }

}

