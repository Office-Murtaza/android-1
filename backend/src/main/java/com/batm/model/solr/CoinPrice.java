package com.batm.model.solr;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.solr.core.mapping.Indexed;
import org.springframework.data.solr.core.mapping.SolrDocument;

import java.util.Date;

@SolrDocument(collection = "coin_price")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CoinPrice {
    @Id
    @Indexed(name = "id", type = "string")
    private String id;

    @Indexed(name = "coinName", type = "string")
    private String coinName;

    @Indexed(name = "price", type = "pdouble")
    private String price;

    @Indexed(name = "timestamp", type = "pdate", defaultValue = "NOW")
    private Date timestamp;
}
