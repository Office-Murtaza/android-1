package com.belco.server.dto;

import com.belco.server.model.CashStatus;
import com.belco.server.model.TransactionStatus;
import com.belco.server.model.TransactionType;
import com.belco.server.util.Constant;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TxDetailsDTO {

    private String txId;
    private String txDbId;
    private String link;
    private BigDecimal cryptoAmount;
    private BigDecimal fiatAmount;
    private BigDecimal cryptoFee;
    private String fromAddress;
    private String toAddress;
    private String fromPhone;
    private String toPhone;
    private String imageId;
    private String message;
    private String sellInfo;
    private Integer confirmations;
    private String refTxId;
    private String refLink;
    private String refCoin;
    private BigDecimal refCryptoAmount;

    @JsonFormat(shape = JsonFormat.Shape.OBJECT)
    private TransactionType type;

    @JsonFormat(shape = JsonFormat.Shape.OBJECT)
    private TransactionStatus status;

    @JsonFormat(shape = JsonFormat.Shape.OBJECT)
    private CashStatus cashStatus;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Constant.SHORT_DATE_FORMAT)
    private Date date1;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Constant.LONG_DATE_FORMAT)
    private Date date2;

    public TxDetailsDTO(String txId, BigDecimal cryptoAmount, TransactionType type, TransactionStatus status, Date date1) {
        this.txId = txId;
        this.cryptoAmount = cryptoAmount;
        this.type = type;
        this.status = status;
        this.date1 = date1;
    }

    public TxDetailsDTO(String txId, BigDecimal cryptoAmount, String fromAddress, String toAddress, TransactionType type, TransactionStatus status, Date date1) {
        this.txId = txId;
        this.cryptoAmount = cryptoAmount;
        this.fromAddress = fromAddress;
        this.toAddress = toAddress;
        this.type = type;
        this.status = status;
        this.date1 = date1;
    }
}