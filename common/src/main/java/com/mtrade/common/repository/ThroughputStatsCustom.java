package com.mtrade.common.repository;

import org.springframework.cache.annotation.Cacheable;

import java.util.List;
import java.util.Map;

/**
 * @author jaroslav.psenicka@gmail.com
 */
public interface ThroughputStatsCustom {

    @Cacheable("stats")
    Map<String, Float> actualStats(String countryCode);

    @Cacheable("throughput-stats")
    List<List<Number>> throughputStats(String countryCode);
}

