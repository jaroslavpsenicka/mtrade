package com.mtrade.dao.repository;

import java.math.BigInteger;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.repository.CrudRepository;

import com.mtrade.dao.model.User;

/**
 * Repository of users,
 * @author jaroslav.psenicka@gmail.com
 */
public interface UserRepository extends CrudRepository<User, BigInteger> {

    @Cacheable("users")
    User findByName(String key);
}

