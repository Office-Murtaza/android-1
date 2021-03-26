package com.belco.server.dto;

import com.belco.server.model.CashStatus;
import com.belco.server.model.TransactionStatus;
import com.belco.server.model.TransactionType;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TxDetailsDTO {

    private String coin;
    private String txId;
    private Long txDBId;
    private String link;
    private BigDecimal cryptoAmount;
    private BigDecimal cryptoFee;
    private String fromAddress;
    private String toAddress;
    private String fromPhone;
    private String toPhone;
    private String imageId;
    private String message;
    private String swapTxId;
    private String swapLink;
    private String swapCoin;
    private BigDecimal swapCryptoAmount;

    @JsonFormat(shape = JsonFormat.Shape.OBJECT)
    private TransactionType type;

    @JsonFormat(shape = JsonFormat.Shape.OBJECT)
    private TransactionStatus status;

    private Integer confirmations;
    private BigDecimal fiatAmount;

    @JsonFormat(shape = JsonFormat.Shape.OBJECT)
    private CashStatus cashStatus;

    private String sellInfo;
    private long timestamp;

    public TxDetailsDTO(String txId, BigDecimal cryptoAmount, TransactionType type, TransactionStatus status, long timestamp) {
        this.txId = txId;
        this.cryptoAmount = cryptoAmount;
        this.type = type;
        this.status = status;
        this.timestamp = timestamp;
    }

    public TxDetailsDTO(Long txDBId, BigDecimal cryptoAmount, TransactionType type, TransactionStatus status, long timestamp) {
        this.txDBId = txDBId;
        this.cryptoAmount = cryptoAmount;
        this.type = type;
        this.status = status;
        this.timestamp = timestamp;
    }

    public TxDetailsDTO(String txId, BigDecimal cryptoAmount, String fromAddress, String toAddress, TransactionType type, TransactionStatus status, long timestamp) {
        this.txId = txId;
        this.cryptoAmount = cryptoAmount;
        this.fromAddress = fromAddress;
        this.toAddress = toAddress;
        this.type = type;
        this.status = status;
        this.timestamp = timestamp;
    }
}