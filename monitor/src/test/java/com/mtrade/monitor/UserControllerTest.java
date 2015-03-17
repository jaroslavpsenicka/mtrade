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
import org.springframework.cache.CacheManager;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.util.NestedServletException;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.hamcrest.Matchers.contains;
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
public class UserControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private UserRepository userRepository;

    private MockMvc mockMvc;

    @Before
    public void before() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        userRepository.save(new User("usr1"));
    }

    @After
    public void after() {
        userRepository.deleteAll();
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @Test
    public void user() throws Exception {
        authenticate("usr1", "USER");
        mockMvc.perform(get("/user"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("name", is("John Doe")))
            .andExpect(jsonPath("roles[0]", is("USER")));
    }

    @Test
    public void admin() throws Exception {
        authenticate("usr2", "ADMIN");
        mockMvc.perform(get("/user"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("name", is("John Doe")))
            .andExpect(jsonPath("roles[0]", is("ADMIN")));
    }

    @Test
    public void noUser() throws Exception {
        try {
            mockMvc.perform(get("/user"));
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

    private void authenticate(String userName, String role) {
        ArrayList<SimpleGrantedAuthority> roles = new ArrayList<>();
        roles.add(new SimpleGrantedAuthority(role));
        User user = new User(userName);
        user.setDisplayName("John Doe");
        UserDetails principal = new UserDetailServiceImpl.UserDetailsImpl(user);
        Authentication authentication = new UsernamePasswordAuthenticationToken(principal, "pwd", roles);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

}

