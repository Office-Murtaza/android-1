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
public class BlockbookTransactionVinDTO {

    private String txid;
    private Long sequence;
    private Integer n;
    private Boolean isAddress;
    private BigDecimal value;
    private String hex;
    private Integer vout;
    private List<String> addresses;
}
