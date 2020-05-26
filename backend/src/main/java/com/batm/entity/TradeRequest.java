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

    private Integer type;
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
    private BigDecimal requestRate;
    private BigDecimal tradeRate;

    @ManyToOne
    @JoinColumn(name = "coin_id")
    private Coin coin;

    @ManyToOne
    @JoinColumn(name = "trade_id")
    private Trade trade;

    @ManyToOne
    @JoinColumn(name = "request_identity_id")
    private Identity requestIdentity;

    @ManyToOne
    @JoinColumn(name = "trade_identity_id")
    private Identity tradeIdentity;
}