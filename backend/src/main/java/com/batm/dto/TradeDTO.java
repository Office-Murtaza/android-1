package com.batm.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TradeDTO {

    private Long id;
    private Integer type;
    private String paymentMethod;
    private BigDecimal margin;
    private Integer minLimit;
    private Integer maxLimit;
    private String terms;
}