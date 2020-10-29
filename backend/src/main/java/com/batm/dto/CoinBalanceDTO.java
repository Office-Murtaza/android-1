package com.batm.dto;

import java.math.BigDecimal;

import com.batm.util.Util;
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
    private BigDecimal reserved;
    private String reservedStr;
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
        this.balanceStr = Util.convert(balance);
    }

    public void setFiatBalance(BigDecimal fiatBalance) {
        this.fiatBalance = fiatBalance;
        this.fiatBalanceStr = Util.convert(fiatBalance);
    }

    public void setReserved(BigDecimal reserved) {
        this.reserved = reserved;
        this.reservedStr = Util.convert(reserved);
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
        this.priceStr = Util.convert(price);
    }
}