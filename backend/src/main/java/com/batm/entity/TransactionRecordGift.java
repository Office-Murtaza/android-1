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
@Table(name = "transactionrecordgift")
public class TransactionRecordGift {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "tx_id")
    private String txId;

    private Integer type;
    private Integer status;
    private BigDecimal amount;
    private String phone;
    private String message;
    private String image;
    private Integer step;

    @Column(name = "ref_tx_id")
    private String refTxId;

    @ManyToOne
    @JoinColumn(name = "coin_id")
    private Coin coin;

    @ManyToOne
    @JoinColumn(name = "identity_id")
    private Identity identity;
}