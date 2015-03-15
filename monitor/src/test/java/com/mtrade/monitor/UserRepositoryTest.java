package com.mtrade.monitor;

import com.mtrade.monitor.model.User;
import com.mtrade.monitor.repository.UserRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.validation.ConstraintViolationException;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:/monitor-context.xml", "classpath:/test-context.xml"})
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Before
    public void before() {
        userRepository.deleteAll();
    }

    @After
    public void after() {
    }

    @Test
    public void findByKey() {
        userRepository.save(new User("usr"));
        userRepository.save(new User("usr2"));
        User user = userRepository.findByName("usr");
        assertNotNull(user);
        assertEquals("usr", user.getName());
        assertNotNull(user.getId());
        assertNotNull(user.getCreateDate());
    }

    @Test
    public void unknownKey() {
        User user = userRepository.findByName("usr");
        assertNull(user);
    }

    @Test
    public void withOneRole() {
        userRepository.save(new User("usr", User.ROLE_USER));
        User user = userRepository.findByName("usr");
        assertNotNull(user);
        assertTrue(user.getRoles().contains(User.ROLE_USER));
        assertFalse(user.getRoles().contains(User.ROLE_ADMIN));
    }

    @Test
    public void withTwoRoles() {
        userRepository.save(new User("usr", User.ROLE_USER, User.ROLE_ADMIN));
        User user = userRepository.findByName("usr");
        assertNotNull(user);
        assertTrue(user.getRoles().contains(User.ROLE_USER));
        assertTrue(user.getRoles().contains(User.ROLE_ADMIN));
    }

    @Test(expected = ConstraintViolationException.class)
    public void noKey() {
        userRepository.save(new User(null));
    }

    @Test(expected = DuplicateKeyException.class)
    public void notUniqueKey() {
        userRepository.save(new User("123"));
        userRepository.save(new User("123"));
    }

    @Test(expected = ConstraintViolationException.class)
    public void noRole() {
        userRepository.save(new User("123", (String[])null));
    }

}
