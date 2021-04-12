package com.belco.server.dto;

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
public class TxSubmitDTO {

    private String hex;
    private Integer type;
    private String fromAddress;
    private String toAddress;
    private BigDecimal cryptoAmount;
    private String phone;
    private String message;
    private String image;
    private BigDecimal fiatAmount;
    private String fiatCurrency;
    private String refCoin;
    private BigDecimal refCryptoAmount;
}