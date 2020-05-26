package com.batm.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import org.bson.Document;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CoinPriceListDTO {

    List<Document> dayCoinPrices;
    List<Document> weekCoinPrices;
    List<Document> monthCoinPrices;
    List<Document> threeMonthsCoinPrices;
    List<Document> yearCoinPrices;
}