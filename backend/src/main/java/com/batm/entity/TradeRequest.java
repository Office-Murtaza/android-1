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
@Table(name = "w_traderequest")
public class TradeRequest extends BaseEntity {

    private Integer status;
    private String paymentMethod;
    private BigDecimal margin;
    private BigDecimal price;
    private Long minLimit;
    private Long maxLimit;
    private BigDecimal cryptoAmount;
    private BigDecimal fiatAmount;
    private String terms;
    private String details;
    private Integer buyRate;
    private Integer sellRate;

    @ManyToOne
    @JoinColumn(name = "coin_id")
    private Coin coin;

    @ManyToOne
    @JoinColumn(name = "trade_id")
    private Trade trade;

    @ManyToOne
    @JoinColumn(name = "buy_identity_id")
    private Identity buyIdentity;

    @ManyToOne
    @JoinColumn(name = "sell_identity_id")
    private Identity sellIdentity;
}