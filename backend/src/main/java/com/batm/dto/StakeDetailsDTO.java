package com.batm.dto;

import com.batm.util.Util;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StakeDetailsDTO {

    private boolean exist;

    private BigDecimal amount;
    private String amountStr;
    private BigDecimal rewardAmount;
    private String rewardAmountStr;
    private BigDecimal rewardPercent;
    private String rewardPercentStr;
    private BigDecimal rewardAnnualAmount;
    private String rewardAnnualAmountStr;
    private BigDecimal rewardAnnualPercent;
    private String rewardAnnualPercentStr;

    private Integer days;
    private Integer minDays;

    public void setExist(boolean exist) {
        this.exist = exist;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
        this.amountStr = Util.convert(amount);
    }

    public void setRewardAmount(BigDecimal rewardAmount) {
        this.rewardAmount = rewardAmount;
        this.rewardAmountStr = Util.convert(rewardAmount);
    }

    public void setRewardPercent(BigDecimal rewardPercent) {
        this.rewardPercent = rewardPercent;
        this.rewardPercentStr = Util.convert(rewardPercent);
    }

    public void setRewardAnnualAmount(BigDecimal rewardAnnualAmount) {
        this.rewardAnnualAmount = rewardAnnualAmount;
        this.rewardAnnualAmountStr = Util.convert(rewardAnnualAmount);
    }

    public void setRewardAnnualPercent(BigDecimal rewardAnnualPercent) {
        this.rewardAnnualPercent = rewardAnnualPercent;
        this.rewardAnnualPercentStr = Util.convert(rewardAnnualPercent);
    }

    public void setDays(Integer days) {
        this.days = days;
    }

    public void setMinDays(Integer minDays) {
        this.minDays = minDays;
    }
}