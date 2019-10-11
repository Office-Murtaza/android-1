package com.batm.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "tlimit")
public class Limit implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long limitId;

    @OneToOne(mappedBy = "limit", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private DailyLimit dailyLimit;

    private BigDecimal amount;
    private String currency;
}