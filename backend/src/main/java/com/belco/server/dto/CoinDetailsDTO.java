package com.belco.server.dto;

import com.belco.server.util.Util;
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
    private BigDecimal convertedTxFee;
    private String convertedTxFeeStr;
    private Long byteFee;
    private Long gasPrice;
    private Long gasLimit;
    private Integer scale;
    private BigDecimal platformSwapFee;
    private String platformSwapFeeStr;
    private BigDecimal platformTradeFee;
    private String platformTradeFeeStr;
    private String walletAddress;
    private String contractAddress;

    public void setTxFee(BigDecimal txFee) {
        this.txFee = txFee;
        this.txFeeStr = Util.convert(txFee);
    }

    public void setConvertedTxFee(BigDecimal convertedTxFee) {
        this.convertedTxFee = convertedTxFee;
        this.convertedTxFeeStr = Util.convert(convertedTxFee);
    }

    public void setByteFee(Long byteFee) {
        this.byteFee = byteFee;
    }

    public void setPlatformSwapFee(BigDecimal platformSwapFee) {
        this.platformSwapFee = platformSwapFee;
        this.platformSwapFeeStr = Util.convert(platformSwapFee);
    }

    public void setPlatformTradeFee(BigDecimal platformTradeFee) {
        this.platformTradeFee = platformTradeFee;
        this.platformTradeFeeStr = Util.convert(platformTradeFee);
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