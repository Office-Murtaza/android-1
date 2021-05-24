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
public class WalletDTO {

    private Boolean valid;
    private BigDecimal price;
    private BigDecimal balance;
    private String walletAddress;
    private String receivingAddress;
    private BigDecimal txFee;
    private BigDecimal convertedTxFee;
    private BigDecimal tolerance;
    private Integer scale;
}