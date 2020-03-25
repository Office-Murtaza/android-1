package com.batm.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WalletDTO {

    private Boolean valid;
    private String address;
    private String newAddress;
    private BigDecimal price;
    private BigDecimal balance;
    private String txId;
    private String fromAddress;
    private String toAddress;
    private BigDecimal amount;
    private Integer confirmations;
    private BigDecimal txFee;
    private BigDecimal byteFee;
    private Long gasPrice;
    private Long gasLimit;
    private BigDecimal txTolerance;
    private Integer scale;
    private List<ReceivedAddressDTO> receivedAddresses;
}