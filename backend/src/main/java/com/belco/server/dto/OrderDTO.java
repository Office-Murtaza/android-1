package com.belco.server.dto;

import com.belco.server.model.OrderStatus;
import com.belco.server.service.CoinService;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderDTO {

    private Long id;
    private Long tradeId;
    private Integer tradeRate;

    @JsonFormat(shape = JsonFormat.Shape.OBJECT)
    private CoinService.CoinEnum coin;

    @JsonFormat(shape = JsonFormat.Shape.OBJECT)
    private OrderStatus status;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createDate;

    private BigDecimal price;
    private BigDecimal cryptoAmount;
    private BigDecimal fiatAmount;
    private String terms;

    private Long makerId;
    private String makerPublicId;
    private BigDecimal makerLatitude;
    private BigDecimal makerLongitude;
    private Integer makerTotalTrades;
    private BigDecimal makerTradingRate;

    private Long takerId;
    private String takerPublicId;
    private BigDecimal takerLatitude;
    private BigDecimal takerLongitude;
    private Integer takerTotalTrades;
    private BigDecimal takerTradingRate;
}