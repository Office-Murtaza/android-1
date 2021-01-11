package com.belco.server.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "w_user")
public class User extends BaseEntity {

    private String phone;
    private String password;
    private String role;
    private String deviceModel;

    @Column(name = "device_os")
    private String deviceOS;

    private String appVersion;
    private Integer platform;
    private String notificationsToken;
    private String byReferralCode;
    private Long tradeCount;
    private BigDecimal tradeRate;
    private BigDecimal latitude;
    private BigDecimal longitude;

    @JsonManagedReference
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<UserCoin> userCoins = new ArrayList<>();

    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Token refreshToken;

    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Unlink unlink;

    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Identity identity;

    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Referral referral;

    @Transient
    public UserCoin getUserCoin(String coinCode) {
        return userCoins.stream().filter(e -> coinCode.equalsIgnoreCase(e.getCoin().getCode())).findFirst().get();
    }
}