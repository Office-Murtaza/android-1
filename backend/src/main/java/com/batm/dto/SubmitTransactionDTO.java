package com.batm.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import net.sf.json.JSONObject;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SubmitTransactionDTO {

    private Integer type;
    private BigDecimal cryptoAmount;
    private Integer fiatAmount;
    private String fiatCurrency;
    private String phone;
    private String message;
    private String imageId;
    private String hex;
    private JSONObject trx;
    private String refTxId;
}