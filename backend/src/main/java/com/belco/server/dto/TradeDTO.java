package com.belco.server.dto;

import com.belco.server.model.TradeStatus;
import com.belco.server.model.TradeType;
import com.belco.server.service.CoinService;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;

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

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm aa, MMMM dd, yyyy")
    private Date createDate;

    private BigDecimal price;
    private BigDecimal minLimit;
    private BigDecimal maxLimit;
    private String paymentMethods;
    private String terms;
    private Integer openOrders;

    private Long makerId;
    private String makerPublicId;
    private BigDecimal makerLatitude;
    private BigDecimal makerLongitude;
    private Integer makerTotalTrades;
    private BigDecimal makerTradingRate;
}