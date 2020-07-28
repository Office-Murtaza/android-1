package com.batm.entity;

import java.math.BigDecimal;
import java.util.List;
import javax.persistence.*;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "w_user")
public class User extends BaseEntity {

    private String phone;
    private String password;
    private String role;
    private Integer platform;
    private Long tradeCount;
    private BigDecimal tradeRate;
    private BigDecimal latitude;
    private BigDecimal longitude;

    @JsonManagedReference
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<UserCoin> userCoins;

    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Token refreshToken;

    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Unlink unlink;

    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Identity identity;

    @Transient
    public UserCoin getUserCoin(String coinCode) {
        return userCoins.stream().filter(e -> coinCode.equalsIgnoreCase(e.getCoin().getCode())).findFirst().get();
    }
}