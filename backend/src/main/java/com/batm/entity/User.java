package com.batm.entity;

import java.io.Serializable;
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
public class User extends AbstractAuditingEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;
    private String phone;
    private String password;
    private String role;

    @JsonManagedReference
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<UserCoin> userCoins;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private CodeVerification codeVerification;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private Token refreshToken;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private Unlink unlink;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private UpdatePhone updatePhone;
}