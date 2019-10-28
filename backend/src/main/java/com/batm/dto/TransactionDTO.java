package com.batm.dto;

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
    private String link;
    private BigDecimal cryptoAmount;
    private BigDecimal cryptoFee;
    private String fromAddress;
    private String toAddress;
    private String imageId;
    private String message;
    private String sellInfo;

    @JsonFormat(shape = JsonFormat.Shape.OBJECT)
    private TransactionType type;

    @JsonFormat(shape = JsonFormat.Shape.OBJECT)
    private TransactionStatus status;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private Date date1;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private Date date2;

    public TransactionDTO(Integer index, String txId, BigDecimal cryptoAmount, TransactionType type, TransactionStatus status, Date date1) {
        this.index = index;
        this.txId = txId;
        this.cryptoAmount = cryptoAmount;
        this.type = type;
        this.status = status;
        this.date1 = date1;
    }
}