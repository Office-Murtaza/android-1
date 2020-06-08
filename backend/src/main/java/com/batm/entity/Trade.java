package com.batm.entity;

import com.batm.dto.TradeDetailsDTO;
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

    @Transient
    private Integer distance = 3;

    @Transient
    private Integer tradeCount = 153;

    @Transient
    private BigDecimal tradeRate = BigDecimal.valueOf(4.3);

    @Transient
    private BigDecimal price;

    @ManyToOne
    @JoinColumn(name = "coin_id")
    private Coin coin;

    @ManyToOne
    @JoinColumn(name = "identity_id")
    private Identity identity;

    @Transient
    public TradeDetailsDTO toDTO() {
        return TradeDetailsDTO.builder()
                .id(getId())
                .type(getType())
                .username(getIdentity().getPublicId())
                .tradeCount(getTradeCount())
                .tradeRate(getTradeRate())
                .distance(getDistance())
                .price(getPrice())
                .paymentMethod(getPaymentMethod())
                .minLimit(getMinLimit())
                .maxLimit(getMaxLimit())
                .terms(getTerms()).build();
    }
}