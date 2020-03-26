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
@Table(name = "w_transactionrecordc2c")
public class TransactionRecordC2C extends BaseTxEntity {

    private BigDecimal refAmount;

    @Column(name = "profit_c2c")
    private BigDecimal profitC2C;

    @Column(name = "ref_tx_id")
    private String refTxId;

    @ManyToOne
    @JoinColumn(name = "ref_coin_id")
    private Coin refCoin;

    @ManyToOne
    @JoinColumn(name = "identity_id")
    private Identity identity;
}