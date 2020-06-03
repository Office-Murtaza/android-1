package com.batm.dto;

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
public class TradeDetailsDTO {

    private Long id;
    private String username;
    private Integer tradeCount;
    private BigDecimal tradeRate;
    private Integer distance;
    private String paymentMethod;
    private BigDecimal price;
    private Long minLimit;
    private Long maxLimit;
    private String terms;
}