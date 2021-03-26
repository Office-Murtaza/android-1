package com.belco.server.dto;

import com.belco.server.model.StakingStatus;
import com.belco.server.util.Util;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StakingDetailsDTO {

    @JsonFormat(shape = JsonFormat.Shape.OBJECT)
    private StakingStatus status;

    private BigDecimal amount;
    private String amountStr;
    private BigDecimal rewardAmount;
    private String rewardAmountStr;
    private BigDecimal rewardPercent;
    private String rewardPercentStr;
    private BigDecimal rewardAnnualAmount;
    private String rewardAnnualAmountStr;
    private int holdPeriod;
    private int annualPercent;
    private long createTimestamp;
    private long cancelTimestamp;
    private int duration;
    private int tillWithdrawal;

    public void setStatus(StakingStatus status) {
        this.status = status;
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

    public void setAnnualPercent(int annualPercent) {
        this.annualPercent = annualPercent;
    }

    public void setHoldPeriod(int holdPeriod) {
        this.holdPeriod = holdPeriod;
    }

    public void setCreateTimestamp(long createTimestamp) {
        this.createTimestamp = createTimestamp;
    }

    public void setCancelTimestamp(long cancelTimestamp) {
        this.cancelTimestamp = cancelTimestamp;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public void setTillWithdrawal(int tillWithdrawal) {
        this.tillWithdrawal = tillWithdrawal;
    }
}