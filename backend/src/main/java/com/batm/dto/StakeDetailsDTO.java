package com.batm.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StakeDetailsDTO {

    private boolean exist;
    private BigDecimal amount;
    private BigDecimal rewardAmount;
    private BigDecimal rewardPercent;
    private BigDecimal rewardAnnualAmount;
    private BigDecimal rewardAnnualPercent;
    private Integer days;
    private Integer minDays;
}