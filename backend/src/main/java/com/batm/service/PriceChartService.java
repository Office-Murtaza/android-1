package com.batm.service;

import com.batm.dto.*;
import com.batm.dto.CoinPriceDTO;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
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
            String coinCode = coinEnum.name().toLowerCase();

            CoinPriceDTO coinPrice = new CoinPriceDTO();
            coinPrice.setPrice(coinEnum.getPrice());
            coinPrice.setTimestamp(System.currentTimeMillis());

            mongo.getCollection("price_day_" + coinCode).createIndex(new Document("timestamp", 1));
            mongo.getCollection("price_week_" + coinCode).createIndex(new Document("timestamp", 1));
            mongo.getCollection("price_month_" + coinCode).createIndex(new Document("timestamp", 1));
            mongo.getCollection("price_three_months_" + coinCode).createIndex(new Document("timestamp", 1));
            mongo.getCollection("price_year_" + coinCode).createIndex(new Document("timestamp", 1));

            Document doc = new Document();
            mongo.getConverter().write(coinPrice, doc);
            long count = mongo.getCollection("price_day_" + coinCode).countDocuments();

            mongo.getCollection("price_day_" + coinCode).insertOne(doc);

            if (count % (7 * 24) == 0) {
                mongo.getCollection("price_week_" + coinCode).insertOne(doc);
            }

            if (count % (30 * 24) == 0) {
                mongo.getCollection("price_month_" + coinCode).insertOne(doc);
            }

            if (count % (90 * 24) == 0) {
                mongo.getCollection("price_three_months_" + coinCode).insertOne(doc);
            }

            if (count % (365 * 24) == 0) {
                mongo.getCollection("price_year_" + coinCode).insertOne(doc);
            }
        });
    }

    @Cacheable(cacheNames = {"priceChart"}, key = "coin")
    public CoinPriceListDTO fetchCoinPrices(String coin) {
        Query query = new Query().with(new Sort(Sort.Direction.DESC, "timestamp")).limit(24);

        List<CoinPriceDTO> dayCoinPrice = mongo.find(query, CoinPriceDTO.class, "price_day_" + coin);
        List<CoinPriceDTO> weekCoinPrice = mongo.find(query, CoinPriceDTO.class, "price_week_" + coin);
        List<CoinPriceDTO> monthCoinPrice = mongo.find(query, CoinPriceDTO.class, "price_month_" + coin);
        List<CoinPriceDTO> threeMonthsCoinPrice = mongo.find(query, CoinPriceDTO.class, "price_three_months_" + coin);
        List<CoinPriceDTO> yearCoinPrice = mongo.find(query, CoinPriceDTO.class, "price_year_" + coin);

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

    private LinkedList<BigDecimal> extractPriceValues(List<CoinPriceDTO> list) {
        return list.size() > 1 ? list.stream()
                .sorted(Comparator.comparing(CoinPriceDTO::getTimestamp))
                .map(it -> it.getPrice())
                .collect(Collectors.toCollection(LinkedList::new)) : new LinkedList<>();
    }
}