package com.belco.server.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.HashMap;
import java.util.Map;

public enum TradeStatus {

    CREATED(1),
    CANCELED(2);

    private static final Map<Integer, TradeStatus> map = new HashMap<>();

    static {
        for (TradeStatus type : TradeStatus.values()) {
            map.put(type.value, type);
        }
    }

    private int value;

    TradeStatus(int value) {
        this.value = value;
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static TradeStatus valueOf(Integer value) {
        return map.get(value);
    }

    @JsonValue
    public int getValue() {
        return value;
    }
}
