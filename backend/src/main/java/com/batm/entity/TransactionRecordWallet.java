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
@Table(name = "w_transactionrecordwallet")
public class TransactionRecordWallet extends BaseTxEntity {

    @OneToOne
    @JoinColumn(name = "coinpath_id")
    private CoinPath coinPath;

    @OneToOne
    @JoinColumn(name = "transactionrecordgift_id")
    private TransactionRecordGift transactionRecordGift;

    @OneToOne
    @JoinColumn(name = "transactionrecordc2c_id")
    private TransactionRecordC2C transactionRecordC2C;
}