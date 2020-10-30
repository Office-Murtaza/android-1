package com.batm.dto;

import com.batm.model.StakeStatus;
import com.batm.util.Util;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.math.BigDecimal;
import java.util.Date;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StakeDetailsDTO {

    private StakeStatus status;
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

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private Date createDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private Date cancelDate;

    private int duration;
    private int untilWithdraw;
    private int holdPeriod;

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

    public void setRewardAnnualPercent(BigDecimal rewardAnnualPercent) {
        this.rewardAnnualPercent = rewardAnnualPercent;
        this.rewardAnnualPercentStr = Util.convert(rewardAnnualPercent);
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

    public void setUntilWithdraw(int untilWithdraw) {
        this.untilWithdraw = untilWithdraw;
    }

    public void setHoldPeriod(int holdPeriod) {
        this.holdPeriod = holdPeriod;
    }
}