package com.batm.entity;

import javax.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "w_phonechange")
public class UpdatePhone extends BaseEntity {

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    private String phone;
    private Integer status = 0;
}