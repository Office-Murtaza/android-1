package com.batm.service;

import com.batm.model.solr.CoinPrice;
import com.batm.repository.solr.CoinPriceRepository;
import org.apache.solr.common.params.FacetParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.solr.core.SolrOperations;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.FacetPage;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
public class SolrService {
    @Autowired
    private SolrOperations solrTemplate;

    @Autowired
    private CoinPriceRepository coinPriceRepository;

    public FacetPage<CoinPrice> collectPriceChartData(CoinService.CoinEnum coinCode) {
        FacetOptions facetOptions = new FacetOptions()
                .addFacetByRange(
                        new FacetOptions.FieldWithDateRangeParameters("timestamp",
                                Date.from(LocalDateTime.now().minusHours(1).atZone(ZoneId.systemDefault()).toInstant()),
                                Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()), "+1MINUTE")
                        .setInclude(FacetParams.FacetRangeInclude.EDGE)
                );
        facetOptions.setFacetMinCount(0);

        SimpleFacetQuery facetQuery = new SimpleFacetQuery(new SimpleStringCriteria("*:*"))
                .setFacetOptions(facetOptions);
        Criteria criteria = new SimpleStringCriteria("coinName:" + coinCode.name());
        facetQuery.addFilterQuery(new SimpleFilterQuery(criteria));
        return solrTemplate.queryForFacetPage("coin_price", facetQuery, CoinPrice.class);
    }

    public void cleanAllCoinPrice () {
        coinPriceRepository.deleteAll();
    }
}
