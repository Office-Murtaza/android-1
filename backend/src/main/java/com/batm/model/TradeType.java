package com.batm.model;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.HashMap;
import java.util.Map;

public enum TradeType {

    BUY(1),
    SELL(2),
    MY(3);

    private int value;

    private static final Map<Integer, TradeType> map = new HashMap<>();

    static {
        for (TradeType type : TradeType.values()) {
            map.put(type.value, type);
        }
    }

    TradeType(int value) {
        this.value = value;
    }

    @JsonValue
    public int getValue() {
        return value;
    }

    public static TradeType valueOf(int value) {
        return map.get(Integer.valueOf(value));
    }
}