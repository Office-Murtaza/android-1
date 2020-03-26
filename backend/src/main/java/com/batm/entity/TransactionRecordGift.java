package com.batm.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import javax.persistence.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "w_transactionrecordgift")
public class TransactionRecordGift extends BaseTxEntity {

    public static final int RECEIVER_NOT_EXIST = 0;
    public static final int RECEIVER_EXIST = 1;

    private Integer receiverStatus;
    private String phone;
    private String imageId;
    private String message;

    @Column(name = "ref_tx_id")
    private String refTxId;

    @ManyToOne
    @JoinColumn(name = "identity_id")
    private Identity identity;
}