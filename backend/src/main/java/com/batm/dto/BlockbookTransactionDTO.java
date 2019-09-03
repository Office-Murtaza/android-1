package com.batm.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class BlockbookTransactionDTO {

    private String address;

    private String txid;
    private Integer version;
    private String blockHash;
    private Integer blockHeight;
    private Integer confirmations;
    private Long blockTime;
    private BigDecimal value;
    private BigDecimal valueIn;
    private BigDecimal fees;
    private String hex;

    private List<BlockbookTransactionVinDTO> vin;
    private List<BlockbookTransactionVoutDTO> vout;
}
