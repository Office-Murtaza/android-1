package com.belco.server.entity;

import com.belco.server.dto.ReferralDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.math.BigDecimal;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "w_referral")
public class Referral extends BaseEntity {

    private String code;
    private int invited;
    private BigDecimal earned;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Transient
    public ReferralDTO toDTO() {
        return new ReferralDTO("http://test.belcobtm.com/" + code, invited, earned);
    }
}