package com.batm.entity;

import javax.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "w_coin")
public class Coin extends BaseEntity {

    private String code;
    private String name;
    private BigDecimal fee;

    @OneToMany
    @JoinColumn(name = "coin_id")
    private List<CoinPath> paths;
}