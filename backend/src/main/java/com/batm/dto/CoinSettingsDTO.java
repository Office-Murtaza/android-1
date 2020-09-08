package com.batm.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CoinSettingsDTO {

    private String code;
    private BigDecimal txFee;
    private String txFeeStr;
    private BigDecimal byteFee;
    private String byteFeeStr;
    private BigDecimal recallFee;
    private String recallFeeStr;
    private Long gasPrice;
    private Long gasLimit;
    private BigDecimal profitExchange;
    private String profitExchangeStr;
    private String walletAddress;
    private String contractAddress;

    public void setTxFee(BigDecimal txFee) {
        this.txFee = txFee;
        this.txFeeStr = txFee.toString();
    }

    public void setByteFee(BigDecimal byteFee) {
        this.byteFee = byteFee;
        this.byteFeeStr = byteFee.toString();
    }

    public void setRecallFee(BigDecimal recallFee) {
        this.recallFee = recallFee;
        this.recallFeeStr = recallFee.toString();
    }

    public void setProfitExchange(BigDecimal profitExchange) {
        this.profitExchange = profitExchange;
        this.profitExchangeStr = profitExchange.toString();
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

    public void setWalletAddress(String walletAddress) {
        this.walletAddress = walletAddress;
    }

    public void setContractAddress(String contractAddress) {
        this.contractAddress = contractAddress;
    }
}