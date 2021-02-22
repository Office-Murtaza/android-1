package com.belco.server.entity;

import com.belco.server.dto.TradeDTO;
import com.belco.server.model.TradeStatus;
import com.belco.server.model.TradeType;
import com.belco.server.service.CoinService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "w_trade")
public class Trade extends BaseEntity {

    private Integer type;
    private Integer status;
    private BigDecimal price;
    private BigDecimal minLimit;
    private BigDecimal maxLimit;
    private BigDecimal lockedCryptoAmount;
    private String paymentMethods;
    private String terms;
    private Integer openOrders;

    @OneToMany(mappedBy = "trade", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Order> orders = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "coin_id")
    private Coin coin;

    @ManyToOne
    @JoinColumn(name = "maker_user_id")
    private User maker;

    private Date createDate = new Date();

    @Transient
    public TradeType getTradeType() {
        return TradeType.valueOf(type);
    }

    @Transient
    public TradeStatus getTradeStatus() {
        return TradeStatus.valueOf(status);
    }

    @Transient
    public TradeDTO toDTO() {
        return new TradeDTO(getId(),
                getTradeType(),
                CoinService.CoinEnum.valueOf(getCoin().getCode()),
                getTradeStatus(),
                getCreateDate(),
                getCreateDate().getTime(),
                getPrice().stripTrailingZeros(),
                getMinLimit().stripTrailingZeros(),
                getMaxLimit().stripTrailingZeros(),
                getPaymentMethods(),
                getTerms(),
                getOpenOrders(),
                getMaker().getId(),
                getMaker().getIdentity().getPublicId(),
                getMaker().getLatitude(),
                getMaker().getLongitude(),
                getMaker().getTotalTrades(),
                getMaker().getTradingRate());
    }
}