package com.belco.server.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "w_terminallocation")
public class TerminalLocation extends BaseEntity {

    private String name;
    private String address;
    private BigDecimal latitude;
    private BigDecimal longitude;

    @OneToMany
    @JoinColumn(name = "terminal_location_id")
    private List<TerminalLocationHour> hours;
}