package com.mtrade.monitor;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mtrade.common.model.ExchangeStats;
import com.mtrade.common.repository.ExchangeStatsRepository;
import com.mtrade.common.repository.ThroughputStatsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

/**
 * @author jaroslav.psenicka@gmail.com
 */
@Controller
public class StatsController {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private ThroughputStatsRepository tputStatsRepository;

    @Autowired
    private ExchangeStatsRepository xchgStatsRepository;

    @ResponseBody
    @PreAuthorize("hasRole('ADMIN')")
    @RequestMapping(value="/stats", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public StatsResponse getStats(@RequestParam(value = "cc", required = false) String countryCode) {
        PageRequest pageRequest = new PageRequest(0, 5, Sort.Direction.DESC, "createDate", "count");
        List<ExchangeStats> exchangeStats = StringUtils.isEmpty(countryCode) ? xchgStatsRepository.find(pageRequest)
            : xchgStatsRepository.findByOriginatingCountry(countryCode, pageRequest);
        return new StatsResponse(tputStatsRepository.actualStats(countryCode), exchangeStats);
    }

    @ResponseBody
    @PreAuthorize("hasRole('ADMIN')")
    @RequestMapping(value="/stats/tput", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<List<Number>> getThroughputStats(@RequestParam(value = "cc", required = false) String countryCode) {
        return tputStatsRepository.throughputStats(countryCode);
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
