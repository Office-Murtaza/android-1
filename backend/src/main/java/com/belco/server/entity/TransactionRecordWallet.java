package com.belco.server.entity;

import com.belco.server.model.ProcessedType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "w_transactionrecordwallet")
public class TransactionRecordWallet extends BaseEntity {

    public static final int RECEIVER_NOT_EXIST = 0;
    public static final int RECEIVER_EXIST = 1;

    private Integer type;
    private Integer status;
    private Integer processed = ProcessedType.SUCCESS.getValue();
    private BigDecimal amount;
    private BigDecimal refAmount;
    private String txId;
    private Integer receiverStatus;
    private String fromPhone;
    private String toPhone;
    private String imageId;
    private String message;
    private Date createDate = new Date();

    @Column(name = "profit_percent")
    private BigDecimal profitPercent;

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