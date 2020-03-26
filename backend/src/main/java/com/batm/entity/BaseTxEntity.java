package com.batm.entity;

import lombok.Getter;
import lombok.Setter;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import java.math.BigDecimal;

@Getter
@Setter
@MappedSuperclass
public abstract class BaseTxEntity extends BaseEntity {

    private Integer type;
    private Integer status;
    private BigDecimal amount;
    private String txId;

    @ManyToOne
    @JoinColumn(name = "coin_id")
    private Coin coin;
}