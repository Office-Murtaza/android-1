package com.belco.server.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;

@Document(collection = "transaction")
@Setter
@Getter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TxDetailsDTO {

    @Id
    private String txId;

    @Indexed
    private String coin;

    @Indexed
    private Long userId;

    private Long txDBId;
    private String link;
    private BigDecimal cryptoAmount;
    private BigDecimal cryptoFee;

    @Indexed
    private String fromAddress;

    @Indexed
    private String toAddress;

    private String fromPhone;
    private String toPhone;
    private String image;
    private String message;

    private String refTxId;
    private String refLink;
    private String refCoin;
    private BigDecimal refCryptoAmount;

    private Integer type;
    private Integer status;

    private Integer confirmations;
    private BigDecimal fiatAmount;
    private Integer cashStatus;
    private String sellInfo;
    private Integer processed;

    private long timestamp;

    public TxDetailsDTO(String txId, BigDecimal cryptoAmount, Integer type, Integer status, long timestamp) {
        this.txId = txId;
        this.cryptoAmount = cryptoAmount;
        this.type = type;
        this.status = status;
        this.timestamp = timestamp;
    }

    public TxDetailsDTO(Long txDBId, BigDecimal cryptoAmount, Integer type, Integer status, long timestamp) {
        this.txDBId = txDBId;
        this.cryptoAmount = cryptoAmount;
        this.type = type;
        this.status = status;
        this.timestamp = timestamp;
    }

    public TxDetailsDTO(String txId, BigDecimal cryptoAmount, String fromAddress, String toAddress, Integer type, Integer status, long timestamp) {
        this.txId = txId;
        this.cryptoAmount = cryptoAmount;
        this.fromAddress = fromAddress;
        this.toAddress = toAddress;
        this.type = type;
        this.status = status;
        this.timestamp = timestamp;
    }
}