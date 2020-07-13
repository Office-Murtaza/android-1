package com.batm.dto;

import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CoinBalanceDTO {

    private Long id;
    private String code;
    private Integer order;
    private String address;
    private BigDecimal balance;
    private BigDecimal reservedBalance;
    private BigDecimal price;
}