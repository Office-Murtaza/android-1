package com.batm.service;

import com.batm.dto.*;
import org.bson.Document;
import org.bson.types.Decimal128;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
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

    @Scheduled(cron = "0 0 */1 * * *") // every 1 hour
    public void storePriceChart() {
        Arrays.stream(CoinService.CoinEnum.values()).forEach(coinEnum -> {
            String coin = coinEnum.name().toLowerCase();

            Document doc = new Document();
            doc.put("price", coinEnum.getPrice());
            doc.put("timestamp", System.currentTimeMillis());

            long count = mongo.getCollection(getPriceDayColl(coin)).countDocuments();

            mongo.getCollection(getPriceDayColl(coin)).insertOne(doc);

            if (count % (7 * 24) == 0) {
                mongo.getCollection(getPriceWeekColl(coin)).insertOne(doc);
            }

            if (count % (30 * 24) == 0) {
                mongo.getCollection(getPriceMonthColl(coin)).insertOne(doc);
            }

            if (count % (90 * 24) == 0) {
                mongo.getCollection(getPrice3MonthColl(coin)).insertOne(doc);
            }

            if (count % (365 * 24) == 0) {
                mongo.getCollection(getPriceYearColl(coin)).insertOne(doc);
            }
        });
    }

    //@Cacheable(cacheNames = {"priceChart"}, key = "coin")
    public CoinPriceListDTO fetchCoinPrices(String coin) {
        Document sort = new Document("timestamp", 1);
        int limit = 24;

        List<Document> dayCoinPrice = mongo.getCollection(getPriceDayColl(coin))
                .find()
                .sort(sort)
                .limit(limit)
                .into(new ArrayList<>());

        List<Document> weekCoinPrice = mongo.getCollection(getPriceWeekColl(coin))
                .find()
                .sort(sort)
                .limit(limit)
                .into(new ArrayList<>());

        List<Document> monthCoinPrice = mongo.getCollection(getPriceMonthColl(coin))
                .find()
                .sort(sort)
                .limit(limit)
                .into(new ArrayList<>());

        List<Document> threeMonthsCoinPrice = mongo.getCollection(getPrice3MonthColl(coin))
                .find()
                .sort(sort)
                .limit(limit)
                .into(new ArrayList<>());

        List<Document> yearCoinPrice = mongo.getCollection(getPriceYearColl(coin))
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

    public ChartPriceDTO getPriceChart(Long userId, CoinService.CoinEnum coinCode) {
        CoinBalanceDTO coinBalanceDTO = coinService.getCoinsBalance(userId, Arrays.asList(coinCode.name())).getCoins().get(0);

        return ChartPriceDTO.builder()
                .price(coinBalanceDTO.getPrice().getUsd())
                .balance(coinBalanceDTO.getBalance())
                .chart(buildPriceChart(coinCode, coinBalanceDTO.getPrice().getUsd()))
                .build();
    }

    private ChartDTO buildPriceChart(CoinService.CoinEnum coinCode, BigDecimal currentPrice) {
        CoinPriceListDTO coinPrices = fetchCoinPrices(coinCode.name().toLowerCase());

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

    private String getPriceDayColl(String coin) {
        return "price_day_" + coin;
    }

    private String getPriceWeekColl(String coin) {
        return "price_week_" + coin;
    }

    private String getPriceMonthColl(String coin) {
        return "price_month_" + coin;
    }

    private String getPrice3MonthColl(String coin) {
        return "price_three_months_" + coin;
    }

    private String getPriceYearColl(String coin) {
        return "price_year_" + coin;
    }
}