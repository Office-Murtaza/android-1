package com.batm.dto;

import com.batm.service.CoinService;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExchangeC2CDTO {

    private String hex;
    private CoinService.CoinEnum coinCode;
    private BigDecimal amount;
    private CoinService.CoinEnum refCoinCode;
    private BigDecimal refAmount;
    private BigDecimal profitC2C;
}