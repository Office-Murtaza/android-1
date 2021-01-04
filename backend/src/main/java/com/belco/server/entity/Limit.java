package com.belco.server.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.math.BigDecimal;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tlimit")
public class Limit extends BaseEntity {

    @Column(name = "currency")
    private String currency;

    @Column(name = "amount")
    private BigDecimal amount;
}