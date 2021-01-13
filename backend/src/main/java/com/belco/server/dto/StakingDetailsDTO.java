package com.belco.server.dto;

import com.belco.server.model.StakeStatus;
import com.belco.server.util.Util;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StakingDetailsDTO {

    private StakeStatus status;
    private BigDecimal amount;
    private String amountStr;
    private BigDecimal rewardAmount;
    private String rewardAmountStr;
    private BigDecimal rewardPercent;
    private String rewardPercentStr;
    private BigDecimal rewardAnnualAmount;
    private String rewardAnnualAmountStr;
    private int rewardAnnualPercent;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private Date createDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private Date cancelDate;

    private int duration;
    private int tillWithdrawal;
    private int cancelHoldPeriod;

    public void setStatus(StakeStatus status) {
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

    public void setRewardAnnualPercent(int rewardAnnualPercent) {
        this.rewardAnnualPercent = rewardAnnualPercent;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public void setCancelDate(Date cancelDate) {
        this.cancelDate = cancelDate;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public void setTillWithdrawal(int tillWithdrawal) {
        this.tillWithdrawal = tillWithdrawal;
    }

    public void setCancelHoldPeriod(int cancelHoldPeriod) {
        this.cancelHoldPeriod = cancelHoldPeriod;
    }
}