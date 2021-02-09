package com.belco.server.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.HashMap;
import java.util.Map;

public enum TradeType {

    BUY(1),
    SELL(2);

    private static final Map<Integer, TradeType> map = new HashMap<>();

    static {
        for (TradeType type : TradeType.values()) {
            map.put(type.value, type);
        }
    }

    private int value;

    TradeType(int value) {
        this.value = value;
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static TradeType valueOf(Integer value) {
        return map.get(value);
    }

    @JsonValue
    public int getValue() {
        return value;
    }
}