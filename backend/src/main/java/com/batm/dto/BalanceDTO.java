package com.batm.dto;

import java.math.BigDecimal;
import java.util.List;
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
public class BalanceDTO {

    private BigDecimal totalBalance;
    private List<CoinBalanceDTO> coins;
}