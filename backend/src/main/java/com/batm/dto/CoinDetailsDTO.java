package com.batm.dto;

import com.batm.util.Util;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CoinDetailsDTO {

    private String code;
    private BigDecimal txFee;
    private String txFeeStr;
    private Long byteFee;
    private BigDecimal recallFee;
    private String recallFeeStr;
    private Long gasPrice;
    private Long gasLimit;
    private Integer scale;
    private BigDecimal profitExchange;
    private String profitExchangeStr;
    private String walletAddress;
    private String contractAddress;

    public void setTxFee(BigDecimal txFee) {
        this.txFee = txFee;
        this.txFeeStr = Util.convert(txFee);
    }

    public void setByteFee(Long byteFee) {
        this.byteFee = byteFee;
    }

    public void setRecallFee(BigDecimal recallFee) {
        this.recallFee = recallFee;
        this.recallFeeStr = Util.convert(recallFee);
    }

    public void setProfitExchange(BigDecimal profitExchange) {
        this.profitExchange = profitExchange;
        this.profitExchangeStr = Util.convert(profitExchange);
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setGasPrice(Long gasPrice) {
        this.gasPrice = gasPrice;
    }

    public void setGasLimit(Long gasLimit) {
        this.gasLimit = gasLimit;
    }

    public void setScale(Integer scale) {
        this.scale = scale;
    }

    public void setWalletAddress(String walletAddress) {
        this.walletAddress = walletAddress;
    }

    public void setContractAddress(String contractAddress) {
        this.contractAddress = contractAddress;
    }
}