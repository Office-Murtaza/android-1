package com.belco.server.entity;

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
@Table(name = "w_token")
public class Token extends BaseEntity {

    private String refreshToken;
    private String accessToken;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;
}