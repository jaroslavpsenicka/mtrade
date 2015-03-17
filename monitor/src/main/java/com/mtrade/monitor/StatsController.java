package com.mtrade.monitor;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mtrade.common.model.ExchangeStats;
import com.mtrade.common.model.ThroughputStats;
import com.mtrade.common.model.StatsType;
import com.mtrade.common.repository.ExchangeStatsRepository;
import com.mtrade.common.repository.ThroughputStatsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

/**
 * @author jaroslav.psenicka@gmail.com
 */
@Controller
public class StatsController {

    @Autowired
    private ThroughputStatsRepository tputStatsRepository;

    @Autowired
    private ExchangeStatsRepository xchgStatsRepository;

    @ResponseBody
    @PreAuthorize("hasRole('ADMIN')")
    @RequestMapping(value="/stats", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public StatsResponse getOverallStats() {
        Map<String, Float> tputResults = new HashMap<>();
        for (ThroughputStats stats : tputStatsRepository.findByTypeOrderByCreateDateDesc(StatsType.OVERALL)) {
            tputResults.put(stats.getCountryCode(), stats.getCount());
        }

        PageRequest pageRequest = new PageRequest(0, 5, Sort.Direction.DESC, "createDate");
        List<ExchangeStats> xchgResults = xchgStatsRepository.find(pageRequest);

        return new StatsResponse(tputResults, xchgResults);
    }

    @ResponseBody
    @PreAuthorize("hasRole('ADMIN')")
    @RequestMapping(value="/stats/tput", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<List<Number>> getThroughputStats() {
        List<List<Number>> results = new ArrayList<>();
        for (ThroughputStats stats : tputStatsRepository.findFirst30ByTypeOrderByCreateDateDesc(StatsType.DAY)) {
            results.add(0, Arrays.asList((Number) stats.getCreateDate().getTime(), stats.getCount()));
        }

        return results;
    }

    @ResponseBody
    @PreAuthorize("hasRole('ADMIN')")
    @RequestMapping(value="/stats/tput/{countryCode}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<List<Number>> getThroughputCountryStats(@PathVariable("countryCode") String countryCode) {
        List<List<Number>> results = new ArrayList<>();
        for (ThroughputStats stats : tputStatsRepository.findFirst30ByTypeAndCountryCodeOrderByCreateDateDesc(StatsType.DAY, countryCode)) {
            results.add(0, Arrays.asList((Number)stats.getCreateDate().getTime(), stats.getCount()));
        }

        return results;
    }

    private static class StatsResponse {

        @JsonProperty("tput")
        private Map<String, Float> tputStats;

        @JsonProperty("xchg")
        private List<ExchangeStats> exchangeStats;

        public StatsResponse(Map<String, Float> tputStats, List<ExchangeStats> exchangeStats) {
            this.tputStats = tputStats;
            this.exchangeStats = exchangeStats;
        }

        public Map<String, Float> getTputStats() {
            return tputStats;
        }

        public List<ExchangeStats> getExchangeStats() {
            return exchangeStats;
        }
    }
}
