package com.batm.entity;

import javax.persistence.*;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "w_coin")
public class Coin extends BaseEntity {

    private String code;
    private String name;
    private Integer order;
    private BigDecimal fee;
    private BigDecimal recallFee;
    private Long gasLimit;
    private Long gasPrice;
    private BigDecimal tolerance;
    private Integer scale;

    @Column(name = "profit_exchange")
    private BigDecimal profitExchange;
}