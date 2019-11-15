package com.batm.entity;

import javax.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "w_codeverify")
public class CodeVerification extends BaseEntity {

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    private String code;
    private Integer status;

    private Date updateDate;
}