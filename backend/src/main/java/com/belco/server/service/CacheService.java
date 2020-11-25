package com.belco.server.service;

import com.belco.server.model.PricePeriod;
import com.belco.server.util.Util;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.Instant;

@Service
public class CacheService {

    @Autowired
    private RestTemplate rest;

    @Value("${coingecko.api.url}")
    private String apiUrl;

    @Cacheable(cacheNames = {"price"}, key = "#id")
    public BigDecimal getPriceById(String id) {
        try {
            JSONObject res = rest.getForObject(apiUrl + "/api/v3/simple/price?ids=" + id + "&vs_currencies=usd", JSONObject.class);

            return Util.format(Util.convert(res.optJSONObject(id).optString("usd")), 3);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return BigDecimal.ZERO;
    }

    @Cacheable(cacheNames = {"price-chart"}, key = "{#id, #period}")
    public JSONArray getPriceChartById(String id, PricePeriod period) {
        try {
            JSONObject res = rest.getForObject(apiUrl + "/api/v3/coins/" + id + "/market_chart/range?vs_currency=usd&from=" + period.getFrom() + "&to=" + Instant.now().getEpochSecond(), JSONObject.class);

            return res.optJSONArray("prices");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new JSONArray();
    }
}