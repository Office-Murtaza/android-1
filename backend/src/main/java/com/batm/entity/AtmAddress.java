package com.batm.entity;

import java.math.BigDecimal;
import java.util.List;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "w_atm_address")
public class AtmAddress extends BaseEntity {

    private String name;
    private String address;
    private BigDecimal latitude;
    private BigDecimal longitude;

    @OneToMany(mappedBy = "atmAddress")
    private List<OpenHour> openHours;
}