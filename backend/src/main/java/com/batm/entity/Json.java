package com.batm.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Json {

    private Long userId;
    private Boolean login;

    public static Json register(Long userId) {
        return new Json(userId, null);
    }

    public static Json login(Boolean login) {
        return new Json(null, login);
    }
}