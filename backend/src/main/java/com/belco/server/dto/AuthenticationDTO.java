package com.belco.server.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthenticationDTO {

    private String phone;
    private String password;
    private Integer platform;
    private String deviceModel;
    private String deviceOS;
    private String appVersion;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String timezone;
    private String notificationsToken;
    private String byReferralCode;
    private List<CoinDTO> coins;
}