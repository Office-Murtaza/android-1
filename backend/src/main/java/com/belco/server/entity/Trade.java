package com.belco.server.entity;

import com.belco.server.dto.TradeDetailsDTO;
import com.belco.server.dto.TradeUserDTO;
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
    private BigDecimal price;

    @ManyToOne
    @JoinColumn(name = "coin_id")
    private Coin coin;

    @ManyToOne
    @JoinColumn(name = "identity_id")
    private Identity identity;

    @Transient
    public TradeDetailsDTO toDTO(TradeUserDTO trader) {
        return TradeDetailsDTO.builder()
                .id(getId())
                .type(getType())
                .trader(trader)
                .price(getPrice())
                .paymentMethod(getPaymentMethod())
                .minLimit(getMinLimit())
                .maxLimit(getMaxLimit())
                .terms(getTerms()).build();
    }
}