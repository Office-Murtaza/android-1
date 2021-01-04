package com.belco.server.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.math.BigDecimal;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "w_coin")
public class Coin extends BaseEntity {

    private String code;
    private String name;
    private Integer idx;
    private BigDecimal tolerance;
    private Integer scale;
}