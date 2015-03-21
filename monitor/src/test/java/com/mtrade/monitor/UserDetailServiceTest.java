package com.mtrade.monitor;

import com.mtrade.monitor.model.User;
import com.mtrade.monitor.repository.UserRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import static org.junit.Assert.*;

/**
 * @author jaroslav.psenicka@gmail.com
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:/monitor-context.xml", "classpath:/security-context.xml", "classpath:/test-context.xml"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD) // to allow admin user to be auto-created
@WebAppConfiguration
public class UserDetailServiceTest {

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private UserRepository userRepository;

    @Before
    public void before() {
        userRepository.save(new User("usr1"));
    }

    @After
    public void after() {
        userRepository.deleteAll();
    }

    @Test
    public void loadByName() throws Exception {
        UserDetails userDetails = userDetailsService.loadUserByUsername("usr1");
        assertNotNull(userDetails);
        assertEquals("usr1", userDetails.getUsername());
        assertNull(userDetails.getPassword());
        assertEquals("[USER]", userDetails.getAuthorities().toString());
        assertTrue(userDetails.isAccountNonExpired());
        assertTrue(userDetails.isAccountNonLocked());
        assertTrue(userDetails.isCredentialsNonExpired());
        assertTrue(userDetails.isEnabled());
    }

    @Test
    public void adminAutoCreate() throws Exception {
        assertNotNull(userDetailsService.loadUserByUsername("admin"));
    }

}

