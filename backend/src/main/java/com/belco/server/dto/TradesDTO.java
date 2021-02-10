package com.belco.server.dto;

import com.belco.server.model.VerificationStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
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
public class TradesDTO {

    private String publicId;

    @JsonFormat(shape = JsonFormat.Shape.OBJECT)
    private VerificationStatus status;

    private Integer totalTrades;
    private BigDecimal tradingRate;
    private List<TradeDTO> trades;
    private List<OrderDTO> orders;
}