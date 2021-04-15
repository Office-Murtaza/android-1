package com.belco.server.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;

@Document(collection = "order")
@Setter
@Getter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderDetailsDTO {

    @Id
    private String id;

    @Indexed
    private String tradeId;

    @Indexed
    private String coin;

    private Integer status;
    private BigDecimal price;
    private BigDecimal cryptoAmount;
    private BigDecimal fiatAmount;
    private String terms;

    private Long makerUserId;
    private String makerPublicId;
    private Integer makerStatus;
    private BigDecimal makerLatitude;
    private BigDecimal makerLongitude;
    private Integer makerTotalTrades;
    private BigDecimal makerTradingRate;
    private Integer makerRate;

    private Long takerUserId;
    private String takerPublicId;
    private Integer takerStatus;
    private BigDecimal takerLatitude;
    private BigDecimal takerLongitude;
    private Integer takerTotalTrades;
    private BigDecimal takerTradingRate;
    private Integer takerRate;

    private Long timestamp;
}