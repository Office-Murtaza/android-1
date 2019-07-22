package com.batm.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CoinBalanceDTO {

    private String coinId;
    private String publicKey;
    private BigDecimal balance;
    private Price price;
    private transient Integer orderIndex;
}