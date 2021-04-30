package com.belco.server.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TokenDTO {

    private Long userId;
    private Long identityId;
    private String accessToken;
    private Long expires;
    private String refreshToken;
    private String firebaseToken;
    private List<String> roles;
    private BalanceDTO balance;
}