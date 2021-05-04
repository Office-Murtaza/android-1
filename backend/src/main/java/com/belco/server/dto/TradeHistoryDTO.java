package com.belco.server.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TradeHistoryDTO {

    private String makerPublicId;
    private Integer makerStatus;
    private Integer makerTotalTrades;
    private BigDecimal makerTradingRate;

    private List<TradeDetailsDTO> trades;
    private List<OrderDetailsDTO> orders;
    private List<OrderMessageDTO> messages;
}