package com.batm.dto;

import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CoinBalanceDTO {

    private Long id;
    private String code;
    private Integer idx;
    private String address;
    private BigDecimal balance;
    private String balanceStr;
    private BigDecimal fiatBalance;
    private String fiatBalanceStr;
    private BigDecimal reservedBalance;
    private String reservedBalanceStr;
    private BigDecimal reservedFiatBalance;
    private String reservedFiatBalanceStr;
    private BigDecimal price;
    private String priceStr;

    public void setId(Long id) {
        this.id = id;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setIdx(Integer idx) {
        this.idx = idx;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
        this.balanceStr = balance == null ? null : balance.toString();
    }

    public void setFiatBalance(BigDecimal fiatBalance) {
        this.fiatBalance = fiatBalance;
        this.fiatBalanceStr = fiatBalance == null ? null : fiatBalance.toString();
    }

    public void setReservedBalance(BigDecimal reservedBalance) {
        this.reservedBalance = reservedBalance;
        this.reservedBalanceStr = reservedBalance == null ? null : reservedBalance.toString();
    }

    public void setReservedFiatBalance(BigDecimal reservedFiatBalance) {
        this.reservedFiatBalance = reservedFiatBalance;
        this.reservedFiatBalanceStr = reservedFiatBalance == null ? null : reservedFiatBalance.toString();
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
        this.priceStr = price == null ? null : price.toString();
    }
}