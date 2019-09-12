package com.batm.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class TrongridTransactionDTO {

    private String txID;
    private String code;
    private BigDecimal amount;
    private Long blockTimestamp;
    private String ownerAddress;
    private String toAddress;
}