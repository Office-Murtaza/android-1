package com.batm.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CoinPriceListDTO {

    List<CoinPriceDTO> dayCoinPrices;
    List<CoinPriceDTO> weekCoinPrices;
    List<CoinPriceDTO> monthCoinPrices;
    List<CoinPriceDTO> threeMonthsCoinPrices;
    List<CoinPriceDTO> yearCoinPrices;
}