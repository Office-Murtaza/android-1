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
public class BlockbookTransactionVoutDTO {

    private BigDecimal value;
    private Integer n;
    private Boolean spent;
    private String hex;
    private Boolean isAddress;
    private List<String> addresses;
}
