package com.batm.model.solr;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.solr.core.mapping.Indexed;
import org.springframework.data.solr.core.mapping.SolrDocument;

import java.math.BigDecimal;
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

    @Indexed(name = "coinCode", type = "string")
    private String coinCode;

    @Indexed(name = "price", type = "pdouble")
    private BigDecimal price;

    @Indexed(name = "date", type = "pdate", defaultValue = "NOW")
    private Date date;
}
