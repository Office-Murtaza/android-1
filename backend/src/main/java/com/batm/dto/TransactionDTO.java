package com.batm.dto;

import com.batm.model.CashStatus;
import com.batm.model.TransactionStatus;
import com.batm.model.TransactionType;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TransactionDTO {

    private Integer index;
    private String txId;
    private String txDbId;
    private String link;
    private BigDecimal cryptoAmount;
    private BigDecimal fiatAmount;
    private BigDecimal cryptoFee;
    private String fromAddress;
    private String toAddress;
    private String phone;
    private String imageId;
    private String message;
    private String sellInfo;
    private Integer confirmations;

    @JsonFormat(shape = JsonFormat.Shape.OBJECT)
    private TransactionType type;

    @JsonFormat(shape = JsonFormat.Shape.OBJECT)
    private TransactionStatus status;

    @JsonFormat(shape = JsonFormat.Shape.OBJECT)
    private CashStatus cashStatus;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private Date date1;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private Date date2;

    public TransactionDTO(String txId, BigDecimal cryptoAmount, TransactionType type, TransactionStatus status, Date date1) {
        this.txId = txId;
        this.cryptoAmount = cryptoAmount;
        this.type = type;
        this.status = status;
        this.date1 = date1;
    }

    public TransactionDTO(String txId, BigDecimal cryptoAmount, String fromAddress, String toAddress, TransactionType type, TransactionStatus status, Date date1) {
        this.txId = txId;
        this.cryptoAmount = cryptoAmount;
        this.fromAddress = fromAddress;
        this.toAddress = toAddress;
        this.type = type;
        this.status = status;
        this.date1 = date1;
    }
}