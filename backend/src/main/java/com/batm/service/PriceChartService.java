package com.batm.service;

import com.batm.dto.*;
import com.batm.util.Util;
import org.bson.Document;
import org.bson.types.Decimal128;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
@EnableScheduling
public class PriceChartService {

    @Autowired
    private MongoTemplate mongo;

    @Autowired
    private CoinService coinService;

    @Autowired
    private CacheService cache;

    @Scheduled(cron = "0 0 */1 * * *") // every 1 hour
    public void storePriceChart() {
        Arrays.stream(CoinService.CoinEnum.values()).forEach(coinEnum -> {
            String coin = coinEnum.name().toLowerCase();

            Document doc = new Document();
            doc.put("price", coinEnum.getPrice());
            doc.put("timestamp", System.currentTimeMillis());

            long count = mongo.getCollection(Util.getPriceDayColl(coin)).countDocuments();

            mongo.getCollection(Util.getPriceDayColl(coin)).insertOne(doc);

            if (count % (7 * 24) == 0) {
                mongo.getCollection(Util.getPriceWeekColl(coin)).insertOne(doc);
            }

            if (count % (30 * 24) == 0) {
                mongo.getCollection(Util.getPriceMonthColl(coin)).insertOne(doc);
            }

            if (count % (90 * 24) == 0) {
                mongo.getCollection(Util.getPrice3MonthColl(coin)).insertOne(doc);
            }

            if (count % (365 * 24) == 0) {
                mongo.getCollection(Util.getPriceYearColl(coin)).insertOne(doc);
            }
        });
    }

    public ChartPriceDTO getPriceChart(Long userId, CoinService.CoinEnum coinCode) {
        CoinBalanceDTO coinBalanceDTO = coinService.getCoinsBalance(userId, Arrays.asList(coinCode.name())).getCoins().get(0);

        return ChartPriceDTO.builder()
                .price(coinBalanceDTO.getPrice().getUsd())
                .balance(coinBalanceDTO.getBalance())
                .chart(buildPriceChart(coinCode, coinBalanceDTO.getPrice().getUsd()))
                .build();
    }

    private ChartDTO buildPriceChart(CoinService.CoinEnum coinCode, BigDecimal currentPrice) {
        CoinPriceListDTO coinPrices = cache.fetchCoinPrices(coinCode.name().toLowerCase());

        LinkedList<BigDecimal> dayPrices = extractPriceValues(coinPrices.getDayCoinPrices());
        LinkedList<BigDecimal> weekPrices = extractPriceValues(coinPrices.getWeekCoinPrices());
        LinkedList<BigDecimal> monthPrices = extractPriceValues(coinPrices.getMonthCoinPrices());
        LinkedList<BigDecimal> threeMonthsPrices = extractPriceValues(coinPrices.getThreeMonthsCoinPrices());
        LinkedList<BigDecimal> yearPrices = extractPriceValues(coinPrices.getYearCoinPrices());

        return ChartDTO.builder()
                .day(ChartItemDTO.builder().prices(dayPrices).changes(calcPriceChanges(currentPrice, dayPrices)).build())
                .week(ChartItemDTO.builder().prices(weekPrices).changes(calcPriceChanges(currentPrice, weekPrices)).build())
                .month(ChartItemDTO.builder().prices(monthPrices).changes(calcPriceChanges(currentPrice, monthPrices)).build())
                .threeMonths(ChartItemDTO.builder().prices(threeMonthsPrices).changes(calcPriceChanges(currentPrice, threeMonthsPrices)).build())
                .year(ChartItemDTO.builder().prices(yearPrices).changes(calcPriceChanges(currentPrice, yearPrices)).build())
                .build();
    }

    private BigDecimal calcPriceChanges(BigDecimal currentPrice, LinkedList<BigDecimal> periodPrices) {
        if (periodPrices.isEmpty()) {
            return BigDecimal.ZERO;
        } else {
            return currentPrice.divide(periodPrices.getFirst(), 3, RoundingMode.DOWN)
                    .subtract(BigDecimal.ONE)
                    .multiply(new BigDecimal(100))
                    .setScale(2, RoundingMode.DOWN);
        }
    }

    private LinkedList<BigDecimal> extractPriceValues(List<Document> list) {
        return list.size() > 1 ? list.stream()
                .sorted(Comparator.comparingLong(it -> it.getLong("timestamp")))
                .map(it -> it.get("price", Decimal128.class).bigDecimalValue())
                .collect(Collectors.toCollection(LinkedList::new)) : new LinkedList<>();
    }
}