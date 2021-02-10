package com.belco.server.entity;

import com.belco.server.dto.OrderDTO;
import com.belco.server.model.OrderStatus;
import com.belco.server.service.CoinService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "w_order")
public class Order extends BaseEntity {

    private Integer status;
    private BigDecimal price;
    private BigDecimal cryptoAmount;
    private BigDecimal fiatAmount;
    private String terms;

    @ManyToOne
    @JoinColumn(name = "trade_id")
    private Trade trade;

    @ManyToOne
    @JoinColumn(name = "coin_id")
    private Coin coin;

    @ManyToOne
    @JoinColumn(name = "maker_user_id")
    private User maker;

    private Integer makerRate;

    @ManyToOne
    @JoinColumn(name = "taker_user_id")
    private User taker;

    private Integer takerRate;
    private Date createDate;

    @Transient
    public OrderStatus getOrderStatus() {
        return OrderStatus.valueOf(status);
    }

    @Transient
    public OrderDTO toDTO() {
        return new OrderDTO(getId(),
                getTrade().getId(),
                null,
                CoinService.CoinEnum.valueOf(getCoin().getCode()),
                getOrderStatus(),
                getCreateDate(),
                getPrice().stripTrailingZeros(),
                getCryptoAmount().stripTrailingZeros(),
                getFiatAmount().stripTrailingZeros(),
                getTerms(),
                getMaker().getId(),
                getMaker().getIdentity().getPublicId(),
                getMaker().getLatitude().stripTrailingZeros(),
                getMaker().getLongitude().stripTrailingZeros(),
                getMaker().getTotalTrades(),
                getMaker().getTradingRate(),
                getTaker().getId(),
                getTaker().getIdentity().getPublicId(),
                getTaker().getLatitude().stripTrailingZeros(),
                getTaker().getLongitude().stripTrailingZeros(),
                getTaker().getTotalTrades(),
                getTaker().getTradingRate()
        );
    }
}