package com.mtrade.monitor;

import com.mtrade.common.model.Stats;
import com.mtrade.common.model.StatsType;
import com.mtrade.common.repository.StatsRepository;
import org.springframework.beans.factory.annotation.Autowired;
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
    private StatsRepository statsRepository;

    @ResponseBody
    @PreAuthorize("hasRole('ADMIN')")
    @RequestMapping(value="/stats", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Float> getOverallStats() {
        Map<String, Float> results = new HashMap<>();
        for (Stats stats : statsRepository.findByTypeOrderByCreateDateDesc(StatsType.OVERALL)) {
            results.put(stats.getCountryCode(), stats.getCount());
        }

        return results;
    }

    @ResponseBody
    @PreAuthorize("hasRole('ADMIN')")
    @RequestMapping(value="/stats/{countryCode}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<List<Number>> getCountryStats(@PathVariable("countryCode") String countryCode) {
        List<List<Number>> results = new ArrayList<>();
        for (Stats stats : statsRepository.findFirst30ByTypeAndCountryCodeOrderByCreateDateDesc(StatsType.DAY, countryCode)) {
            results.add(0, Arrays.asList((Number)stats.getCreateDate().getTime(), stats.getCount()));
        }

        return results;
    }

}
