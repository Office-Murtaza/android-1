package com.batm.entity;

import lombok.*;
import javax.persistence.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "w_transactionrecordreserve")
public class TransactionRecordReserve extends BaseTxEntity {

    @ManyToOne
    @JoinColumn(name = "identity_id")
    private Identity identity;
}