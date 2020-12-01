package com.belco.server.entity;

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
@Table(name = "tlimit")
public class Limit extends BaseEntity {

    @Column(name = "currency")
    private String currency;

    @Column(name = "amount")
    private BigDecimal amount;
}