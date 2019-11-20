package com.batm.entity;

import javax.persistence.Entity;
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

    private String days;
    private String hours;
}