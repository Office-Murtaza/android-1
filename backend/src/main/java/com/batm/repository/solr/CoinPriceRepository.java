package com.batm.repository.solr;

import com.batm.model.solr.CoinPrice;
import org.springframework.data.solr.repository.SolrCrudRepository;

public interface CoinPriceRepository extends SolrCrudRepository<CoinPrice, String> {

}
