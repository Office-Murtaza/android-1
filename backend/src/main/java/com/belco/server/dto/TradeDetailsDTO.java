package com.belco.server.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;

@Document(collection = "trade")
@Setter
@Getter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TradeDetailsDTO {

    @Id
    private String id;

    @Indexed
    private String coin;

    private Integer type;
    private Integer status;
    private BigDecimal price;
    private BigDecimal minLimit;
    private BigDecimal maxLimit;
    private BigDecimal lockedCryptoAmount;
    private String paymentMethods;
    private String terms;
    private Integer openOrders;

    @Indexed
    private Long makerUserId;
    private String makerPublicId;
    private Integer makerStatus;
    private BigDecimal makerLatitude;
    private BigDecimal makerLongitude;
    private Integer makerTotalTrades;
    private BigDecimal makerTradingRate;

    private Long timestamp;
}
