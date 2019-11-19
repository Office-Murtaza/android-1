package com.batm.entity;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
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
@Table(name = "w_terminallocationhour")
public class TerminalLocationHour extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "terminal_location_id")
    private TerminalLocation terminalLocation;

    private String days;
    private String hours;
}