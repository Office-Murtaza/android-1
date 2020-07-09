package com.batm.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TokenDTO {

    private Long userId;
    private Long identityId;
    private String accessToken;
    private Long expires;
    private String refreshToken;
    private List<String> roles;
    private BalanceDTO balance;

    public TokenDTO(Long userId, Long identityId, String accessToken, Long expires, String refreshToken, List<String> roles) {
        this.userId = userId;
        this.identityId = identityId;
        this.accessToken = accessToken;
        this.expires = expires;
        this.refreshToken = refreshToken;
        this.roles = roles;
    }
}