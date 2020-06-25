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
@Table(name = "w_transactionrecordwallet")
public class TransactionRecordWallet extends BaseEntity {

    public static final int RECEIVER_NOT_EXIST = 0;
    public static final int RECEIVER_EXIST = 1;

    private Integer type;
    private Integer status;
    private BigDecimal amount;
    private BigDecimal refAmount;
    private String txId;
    private Integer receiverStatus;
    private String phone;
    private String imageId;
    private String message;

    @Column(name = "profit")
    private BigDecimal profit;

    @Column(name = "ref_tx_id")
    private String refTxId;

    @ManyToOne
    @JoinColumn(name = "coin_id")
    private Coin coin;

    @ManyToOne
    @JoinColumn(name = "ref_coin_id")
    private Coin refCoin;

    @OneToOne
    @JoinColumn(name = "coinpath_id")
    private CoinPath coinPath;

    @ManyToOne
    @JoinColumn(name = "identity_id")
    private Identity identity;
}