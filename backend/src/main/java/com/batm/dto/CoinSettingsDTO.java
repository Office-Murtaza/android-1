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
public class CoinSettingsDTO {

    private String code;
    private BigDecimal txFee;
    private BigDecimal byteFee;
    private Long gasPrice;
    private Long gasLimit;
    private BigDecimal profitC2C;
    private String serverWalletAddress;
}