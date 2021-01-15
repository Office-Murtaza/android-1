package com.belco.server.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SubmitTransactionDTO {

    private Integer type;
    private String fromAddress;
    private String toAddress;
    private BigDecimal cryptoAmount;
    private BigDecimal refCryptoAmount;
    //private BigDecimal fee;
    private Integer fiatAmount;
    private String fiatCurrency;
    private String phone;
    private String message;
    private String imageId;
    private String hex;
    private String refTxId;
    private String refCoin;
    private Boolean fromServerWallet;
}