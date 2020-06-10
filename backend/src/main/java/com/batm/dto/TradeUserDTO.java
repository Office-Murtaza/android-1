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
public class TradeUserDTO {

    private String username;
    private Integer tradeCount;
    private BigDecimal tradeRate;
    private Integer distance;
}