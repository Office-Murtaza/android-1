package com.batm.entity;

import com.batm.model.TradeType;
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
@Table(name = "w_trade")
public class Trade extends BaseEntity {

    private Integer type;
    private String paymentMethod;
    private BigDecimal margin;
    private Long minLimit;
    private Long maxLimit;
    private String terms;

    @ManyToOne
    @JoinColumn(name = "coin_id")
    private Coin coin;

    @ManyToOne
    @JoinColumn(name = "identity_id")
    private Identity identity;

    @Transient
    public TradeType getTradeType() {
        return getType() == 1 ? TradeType.BUY : TradeType.SELL;
    }
}