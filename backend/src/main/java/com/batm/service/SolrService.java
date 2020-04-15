package com.batm.service;

import com.batm.dto.ChartDTO;
import com.batm.dto.ChartItemDTO;
import com.batm.model.solr.CoinPrice;
import com.batm.repository.solr.CoinPriceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.solr.core.SolrOperations;
import org.springframework.data.solr.core.query.Criteria;
import org.springframework.data.solr.core.query.SimpleFilterQuery;
import org.springframework.data.solr.core.query.SimpleQuery;
import org.springframework.data.solr.core.query.SimpleStringCriteria;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SolrService {

    private static final int DAYS_IN_WEEK = 7;
    private static final int DAYS_IN_YEAR = 365;

    @Autowired
    private SolrOperations solrTemplate;

    @Autowired
    private CoinPriceRepository coinPriceRepository;

    @Cacheable(cacheNames = {"priceChart"}, key = "coinCode")
    public Page<CoinPrice> fetchCoinPricesForLastYear(CoinService.CoinEnum coinCode) {
        SimpleQuery simpleQuery = new SimpleQuery(new SimpleStringCriteria("*:*"));
        Criteria criteria = new SimpleStringCriteria("coinCode:" + coinCode.name());
        simpleQuery.addFilterQuery(new SimpleFilterQuery(criteria));
        simpleQuery.addSort(Sort.by(Sort.Direction.DESC, "date"));
        simpleQuery.setRows(DAYS_IN_YEAR * 24); // maximum 1 year

        return solrTemplate.query("coin_price", simpleQuery, CoinPrice.class);
    }

    public ChartDTO collectPriceChartData(CoinService.CoinEnum coinCode, BigDecimal currentPrice) {
        LocalDate now = LocalDate.now();
        YearMonth currentYearMonth = YearMonth.of(now.getYear(), now.getMonth());
        int daysInCurrentMonth = currentYearMonth.lengthOfMonth();
        int daysInLast3Months = daysInCurrentMonth
                + currentYearMonth.minusMonths(1).lengthOfMonth()
                + currentYearMonth.minusMonths(2).lengthOfMonth();

        final Integer[] counter = {1};

        List<CoinPrice> pricesPerDay = new ArrayList<>();
        List<CoinPrice> pricesPerWeek = new ArrayList<>();
        List<CoinPrice> pricesPerMonth = new ArrayList<>();
        List<CoinPrice> pricesPer3Months = new ArrayList<>();
        List<CoinPrice> pricesPerYear = new ArrayList<>();

        Page<CoinPrice> coinPrices = fetchCoinPricesForLastYear(coinCode);
        coinPrices.forEach(coinPrice -> {
            if (counter[0] <= 24) { // day
                pricesPerDay.add(coinPrice);
                if (counter[0] % 7 == 0) {
                    pricesPerWeek.add(coinPrice);
                }
            } else if (counter[0] <= DAYS_IN_WEEK * 24) { // week
                if (counter[0] % DAYS_IN_WEEK == 0) {
                    pricesPerWeek.add(coinPrice);
                }
                if (counter[0] % daysInCurrentMonth == 0) {
                    pricesPerMonth.add(coinPrice);
                }
                if (counter[0] % daysInLast3Months == 0) {
                    pricesPer3Months.add(coinPrice);
                }
            } else if (counter[0] <= daysInCurrentMonth * 24) { // month
                if (counter[0] % daysInCurrentMonth == 0) {
                    pricesPerMonth.add(coinPrice);
                }
                if (counter[0] % daysInLast3Months == 0) {
                    pricesPer3Months.add(coinPrice);
                }
                if (counter[0] % DAYS_IN_YEAR == 0) {
                    pricesPerYear.add(coinPrice);
                }
            } else if (counter[0] <= daysInLast3Months * 24) { // 3 months
                if (counter[0] % daysInLast3Months == 0) {
                    pricesPer3Months.add(coinPrice);
                }
                if (counter[0] % DAYS_IN_YEAR == 0) {
                    pricesPerYear.add(coinPrice);
                }
            } else if (counter[0] <= DAYS_IN_YEAR * 24) { // year
                if (counter[0] % DAYS_IN_YEAR == 0) {
                    pricesPerYear.add(coinPrice);
                }
            }
            counter[0]++;
        });

        LinkedList<BigDecimal> preparedPricesPerDay = pricesPerDay.size() > 1
                ? pricesPerDay.stream()
                .sorted(Comparator.comparing(CoinPrice::getDate))
                .map(it -> new BigDecimal(it.getPrice()))
                .collect(Collectors.toCollection(LinkedList::new))
                : new LinkedList<>();
        LinkedList<BigDecimal> preparedPricesPerWeek = pricesPerWeek.size() > 1
                ? pricesPerWeek.stream()
                .sorted(Comparator.comparing(CoinPrice::getDate))
                .map(it -> new BigDecimal(it.getPrice()))
                .collect(Collectors.toCollection(LinkedList::new))
                : new LinkedList<>();
        LinkedList<BigDecimal> preparedPricesPerMonth = pricesPerMonth.size() > 1
                ? pricesPerMonth.stream()
                .sorted(Comparator.comparing(CoinPrice::getDate))
                .map(it -> new BigDecimal(it.getPrice()))
                .collect(Collectors.toCollection(LinkedList::new))
                : new LinkedList<>();
        LinkedList<BigDecimal> preparedPricesPer3Months = pricesPer3Months.size() > 1
                ? pricesPer3Months.stream()
                .sorted(Comparator.comparing(CoinPrice::getDate))
                .map(it -> new BigDecimal(it.getPrice()))
                .collect(Collectors.toCollection(LinkedList::new))
                : new LinkedList<>();
        LinkedList<BigDecimal> preparedPricesPerYear = pricesPerYear.size() > 1
                ? pricesPerYear.stream()
                .sorted(Comparator.comparing(CoinPrice::getDate))
                .map(it -> new BigDecimal(it.getPrice()))
                .collect(Collectors.toCollection(LinkedList::new))
                : new LinkedList<>();
        return ChartDTO.builder()
                .day(ChartItemDTO.builder()
                        .prices(preparedPricesPerDay)
                        .changes(calcPriceChanges(currentPrice, preparedPricesPerDay))
                        .build())
                .week(ChartItemDTO.builder()
                        .prices(preparedPricesPerWeek)
                        .changes(calcPriceChanges(currentPrice, preparedPricesPerWeek))
                        .build())
                .month(ChartItemDTO.builder()
                        .prices(preparedPricesPerMonth)
                        .changes(calcPriceChanges(currentPrice, preparedPricesPerMonth))
                        .build())
                .threeMonths(ChartItemDTO.builder()
                        .prices(preparedPricesPer3Months)
                        .changes(calcPriceChanges(currentPrice, preparedPricesPer3Months))
                        .build())
                .year(ChartItemDTO.builder()
                        .prices(preparedPricesPerYear)
                        .changes(calcPriceChanges(currentPrice, preparedPricesPerYear))
                        .build())
                .build();
    }

    private BigDecimal calcPriceChanges(BigDecimal current, LinkedList<BigDecimal> periodPrices) {
        if (periodPrices.isEmpty()) {
            return BigDecimal.ZERO;
        } else {
            return current.divide(periodPrices.getFirst(), 5, RoundingMode.HALF_DOWN)
                    .multiply(new BigDecimal(100)).subtract(new BigDecimal(100)).setScale(2, RoundingMode.HALF_DOWN);
        }
    }

    public void deleteAllPrices() {
        coinPriceRepository.deleteAll();
    }
}
