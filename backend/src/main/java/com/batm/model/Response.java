package com.batm.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Response {

    private Object response;
    private Error error;

    public static Response ok(Object res) {
        return new Response(res, null);
    }

    public static Response error(Error error) {
        return new Response(null, error);
    }

    public static Response error(Integer errorCode, String message) {
        return Response.error(new Error(errorCode, message));
    }

    public static Response serverError() {
        return Response.error(new Error(1, "Server error"));
    }
}