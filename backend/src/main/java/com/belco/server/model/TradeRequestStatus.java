package com.belco.server.model;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.HashMap;
import java.util.Map;

public enum TradeRequestStatus {

    NEW(1),
    PAID(2),
    RELEASED(3),
    CANCELED(4),
    DISPUTED(5);

    private int value;

    private static final Map<Integer, TradeRequestStatus> map = new HashMap<>();

    static {
        for (TradeRequestStatus type : TradeRequestStatus.values()) {
            map.put(type.value, type);
        }
    }

    TradeRequestStatus(int value) {
        this.value = value;
    }

    @JsonValue
    public int getValue() {
        return value;
    }

    public static TradeRequestStatus valueOf(Integer value) {
        return map.get(value);
    }
}