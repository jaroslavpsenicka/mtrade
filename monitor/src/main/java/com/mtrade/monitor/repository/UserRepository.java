package com.mtrade.monitor.repository;

import com.mtrade.monitor.model.User;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.math.BigInteger;

/**
 * @author jaroslav.psenicka@gmail.com
 */
public interface UserRepository extends MongoRepository<User, BigInteger> {

    User findByName(String key);
}

