package com.belco.server.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.HashMap;
import java.util.Map;

public enum PaymentMethod {

    CASH(1),
    PAY_PALL(2),
    VENMO(3),
    CASH_APP(4),
    PAYONEER(5);

    private static final Map<Integer, PaymentMethod> map = new HashMap<>();

    static {
        for (PaymentMethod type : PaymentMethod.values()) {
            map.put(type.value, type);
        }
    }

    private int value;

    PaymentMethod(int value) {
        this.value = value;
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static PaymentMethod valueOf(Integer value) {
        return map.get(value);
    }

    @JsonValue
    public int getValue() {
        return value;
    }
}