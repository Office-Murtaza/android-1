package com.batm.dto;

import java.math.BigDecimal;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BalanceDTO {

    private BigDecimal totalBalance;
    private String totalBalanceStr;
    private List<CoinBalanceDTO> coins;

    public void setTotalBalance(BigDecimal totalBalance) {
        this.totalBalance = totalBalance;
        this.totalBalanceStr = totalBalance == null ? null : totalBalance.toString();
    }

    public void setCoins(List<CoinBalanceDTO> coins) {
        this.coins = coins;
    }
}