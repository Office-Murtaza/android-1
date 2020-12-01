package com.belco.server.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.sf.json.JSONObject;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Response {

    private Object response;
    private com.belco.server.model.Error error;

    public static Response ok(Object res) {
        return new Response(res, null);
    }

    public static Response ok(Boolean bool) {
        JSONObject res = new JSONObject();
        res.put("result", bool);

        return new Response(res, null);
    }

    public static Response ok(String key, Object value) {
        JSONObject res = new JSONObject();
        res.put(key, value);

        return new Response(res, null);
    }

    public static Response error(com.belco.server.model.Error error) {
        return new Response(null, error);
    }

    public static Response error(Integer code, String message) {
        return Response.error(new com.belco.server.model.Error(code, message));
    }

    public static Response serverError() {
        return Response.error(new com.belco.server.model.Error(1, "Server error"));
    }

    public static Response defaultError(String message) {
        return Response.error(new com.belco.server.model.Error(2, message));
    }
}