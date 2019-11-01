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

    private String coinId;
    private String publicKey;
    private BigDecimal balance;
    private AmountDTO price;
    private transient Integer orderIndex;
}