package com.belco.server.dto;

import com.belco.server.model.OrderStatus;
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
public class OrderDTO {

    private String id;
    private String tradeId;

    @JsonFormat(shape = JsonFormat.Shape.OBJECT)
    private OrderStatus status;

    private BigDecimal cryptoAmount;
    private BigDecimal fiatAmount;
    private BigDecimal price;
    private Integer rate;
}