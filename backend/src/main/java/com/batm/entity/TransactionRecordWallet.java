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

    private Integer type;
    private Integer status;
    private BigDecimal amount;

    @ManyToOne
    @JoinColumn(name = "coin_id")
    private Coin coin;

    @Column(name = "tx_id")
    private String txId;

    @OneToOne
    @JoinColumn(name = "coinpath_id")
    private CoinPath coinPath;

    @OneToOne
    @JoinColumn(name = "transactionrecordgift_id")
    private TransactionRecordGift transactionRecordGift;
}