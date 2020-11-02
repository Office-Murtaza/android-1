package com.belco.server.entity;

import com.belco.server.dto.TradeRequestDetailsDTO;
import com.belco.server.dto.TradeUserDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "w_traderequest")
public class TradeRequest extends BaseEntity {

    private BigDecimal price;
    private BigDecimal cryptoAmount;
    private BigDecimal fiatAmount;
    private Integer status;
    private String paymentMethod;
    private String terms;
    private String details;
    private Integer buyRate;
    private Integer sellRate;
    private Date createDate;

    @ManyToOne
    @JoinColumn(name = "coin_id")
    private Coin coin;

    @ManyToOne
    @JoinColumn(name = "buy_identity_id")
    private Identity buyIdentity;

    @ManyToOne
    @JoinColumn(name = "sell_identity_id")
    private Identity sellIdentity;

    @Transient
    public TradeRequestDetailsDTO toDTO(TradeUserDTO buyer, TradeUserDTO seller) {
        return TradeRequestDetailsDTO.builder()
                .id(getId())
                .price(getPrice())
                .cryptoAmount(getCryptoAmount())
                .fiatAmount(getFiatAmount())
                .status(getStatus())
                .paymentMethod(getPaymentMethod())
                .terms(getTerms())
                .details(getDetails())
                .buyRate(getBuyRate())
                .sellRate(getSellRate())
                .buyer(buyer)
                .seller(seller)
                .build();
    }
}