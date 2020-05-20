package com.batm.service;

import com.batm.dto.CoinPriceListDTO;
import com.batm.util.Util;
import com.binance.api.client.BinanceApiRestClient;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class CacheService {

    @Autowired
    private BinanceApiRestClient binanceApi;

    @Autowired
    private MongoTemplate mongo;

    @Cacheable(cacheNames = {"price"}, key = "#symbol")
    public BigDecimal getBinancePriceBySymbol(String symbol) {
        return Util.convert(binanceApi.getPrice(symbol).getPrice());
    }

    @Cacheable(cacheNames = {"price-chart"}, key = "#coin")
    public CoinPriceListDTO fetchCoinPrices(String coin) {
        Document sort = new Document("timestamp", 1);
        int limit = 24;

        List<Document> dayCoinPrice = mongo.getCollection(Util.getPriceDayColl(coin))
                .find()
                .sort(sort)
                .limit(limit)
                .into(new ArrayList<>());

        List<Document> weekCoinPrice = mongo.getCollection(Util.getPriceWeekColl(coin))
                .find()
                .sort(sort)
                .limit(limit)
                .into(new ArrayList<>());

        List<Document> monthCoinPrice = mongo.getCollection(Util.getPriceMonthColl(coin))
                .find()
                .sort(sort)
                .limit(limit)
                .into(new ArrayList<>());

        List<Document> threeMonthsCoinPrice = mongo.getCollection(Util.getPrice3MonthColl(coin))
                .find()
                .sort(sort)
                .limit(limit)
                .into(new ArrayList<>());

        List<Document> yearCoinPrice = mongo.getCollection(Util.getPriceYearColl(coin))
                .find()
                .sort(sort)
                .limit(limit)
                .into(new ArrayList<>());

        return CoinPriceListDTO.builder()
                .dayCoinPrices(dayCoinPrice)
                .weekCoinPrices(weekCoinPrice)
                .monthCoinPrices(monthCoinPrice)
                .threeMonthsCoinPrices(threeMonthsCoinPrice)
                .yearCoinPrices(yearCoinPrice)
                .build();
    }
}