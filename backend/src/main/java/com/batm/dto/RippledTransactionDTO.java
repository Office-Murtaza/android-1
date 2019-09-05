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
public class RippledTransactionDTO {

    private String destination;
    private String account;
    private String hash;
    private Long date;
    private Boolean verified;
    private BigDecimal amount;
}
