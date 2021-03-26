package com.belco.server.dto;

import com.belco.server.model.TradeStatus;
import com.belco.server.model.TradeType;
import com.belco.server.model.VerificationStatus;
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

    private Long id;

    @JsonFormat(shape = JsonFormat.Shape.OBJECT)
    private TradeType type;

    @JsonFormat(shape = JsonFormat.Shape.OBJECT)
    private CoinService.CoinEnum coin;

    @JsonFormat(shape = JsonFormat.Shape.OBJECT)
    private TradeStatus status;

    private long timestamp;

    private BigDecimal price;
    private BigDecimal minLimit;
    private BigDecimal maxLimit;
    private String paymentMethods;
    private String terms;
    private Integer openOrders;

    private Long makerId;
    private String makerPublicId;

    @JsonFormat(shape = JsonFormat.Shape.OBJECT)
    private VerificationStatus makerStatus;

    private BigDecimal makerLatitude;
    private BigDecimal makerLongitude;
    private Integer makerTotalTrades;
    private BigDecimal makerTradingRate;
}