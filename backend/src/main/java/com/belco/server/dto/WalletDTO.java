package com.belco.server.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
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