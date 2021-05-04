package com.belco.server.dto;

import com.belco.server.model.TradeType;
import com.belco.server.service.CoinService;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TradeDTO {

    private String id;

    @JsonFormat(shape = JsonFormat.Shape.OBJECT)
    private TradeType type;

    @JsonFormat(shape = JsonFormat.Shape.OBJECT)
    private CoinService.CoinEnum coin;

    private BigDecimal price;
    private BigDecimal minLimit;
    private BigDecimal maxLimit;
    private String paymentMethods;
    private String terms;
}