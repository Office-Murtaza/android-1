package com.belco.server.entity;

import java.math.BigDecimal;
import java.util.List;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
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