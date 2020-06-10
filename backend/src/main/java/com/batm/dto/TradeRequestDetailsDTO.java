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
public class TradeRequestDetailsDTO {

    private Long id;
    private BigDecimal price;
    private BigDecimal cryptoAmount;
    private BigDecimal fiatAmount;
    private Integer status;
    private String paymentMethod;
    private String terms;
    private String details;
    private Integer buyRate;
    private Integer sellRate;
    private TradeUserDTO buyer;
    private TradeUserDTO seller;
}