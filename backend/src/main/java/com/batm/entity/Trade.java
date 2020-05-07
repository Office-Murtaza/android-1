package com.batm.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "w_trade")
public class Trade extends BaseEntity {

    private Integer type;
    private String paymentMethod;
    private BigDecimal margin;
    private Integer minLimit;
    private Integer maxLimit;
    private String terms;

    @ManyToOne
    @JoinColumn(name = "coin_id")
    private Coin coin;

    @ManyToOne
    @JoinColumn(name = "identity_id")
    private Identity identity;
}