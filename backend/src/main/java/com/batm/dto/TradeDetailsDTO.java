package com.batm.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TradeDetailsDTO {

    private Long id;
    private Integer type;
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