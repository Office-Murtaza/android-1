package com.belco.server.entity;

import com.belco.server.model.VerificationStatus;
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
    private Integer status;
    private String role;
    private String deviceModel;

    @Column(name = "device_os")
    private String deviceOS;

    private String appVersion;
    private Integer platform;
    private String notificationsToken;
    private String byReferralCode;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String timezone;
    private Integer totalTrades;
    private BigDecimal tradingRate;

    @JsonManagedReference
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<UserCoin> userCoins = new ArrayList<>();

    @OneToMany(mappedBy = "maker", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Trade> trades = new ArrayList<>();

    @OneToMany(mappedBy = "maker", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Order> madeOrders = new ArrayList<>();

    @OneToMany(mappedBy = "taker", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Order> takenOrders = new ArrayList<>();

    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Token refreshToken;

    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Identity identity;

    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Referral referral;

    @Transient
    public VerificationStatus getVerificationStatus() {
        return VerificationStatus.valueOf(status);
    }

    @Transient
    public UserCoin getUserCoin(String coinCode) {
        return userCoins.stream().filter(e -> coinCode.equalsIgnoreCase(e.getCoin().getCode())).findFirst().get();
    }
}