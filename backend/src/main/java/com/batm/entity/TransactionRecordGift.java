package com.batm.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import javax.persistence.*;
import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "w_transactionrecordgift")
public class TransactionRecordGift extends BaseEntity {

    public static final int RECEIVER_NOT_EXIST = 0;
    public static final int RECEIVER_EXIST = 1;

    private Integer type;
    private Integer status;
    private BigDecimal amount;
    private Integer receiverStatus;
    private String phone;
    private String imageId;
    private String message;

    @Column(name = "tx_id")
    private String txId;

    @Column(name = "ref_tx_id")
    private String refTxId;

    @ManyToOne
    @JoinColumn(name = "coin_id")
    private Coin coin;

    @ManyToOne
    @JoinColumn(name = "identity_id")
    private Identity identity;
}