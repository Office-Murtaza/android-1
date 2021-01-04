package com.belco.server.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.Date;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "w_coinpath")
public class CoinPath extends BaseEntity {

    private String path;
    private String address;

    @ManyToOne(optional = false)
    private Coin coin;

    private Date updateDate;
}