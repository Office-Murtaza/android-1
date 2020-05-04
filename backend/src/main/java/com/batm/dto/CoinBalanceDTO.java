package com.batm.dto;

import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CoinBalanceDTO {

    private Long id;
    private String code;
    private String address;
    private BigDecimal balance;
    private BigDecimal reservedBalance;
    private AmountDTO price;
}