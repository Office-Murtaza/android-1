package com.batm.model.solr;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.solr.core.mapping.Indexed;
import org.springframework.data.solr.core.mapping.SolrDocument;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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

    @Indexed(name = "price", type = "float")
    private BigDecimal price;

    @Indexed(name = "timestamp", type = "dateTime", defaultValue = "NOW")
    private LocalDateTime timestamp;
}
