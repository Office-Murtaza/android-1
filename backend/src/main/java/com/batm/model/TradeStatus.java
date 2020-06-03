package com.batm.model;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.HashMap;
import java.util.Map;

public enum TradeStatus {

    CREATED(1),
    PAID(2),
    RELEASED(3),
    COMPLETE(4),
    CANCELED(5),
    DISPUTED(6),
    EXPIRED(7);

    private int value;

    private static final Map<Integer, TradeStatus> map = new HashMap<>();

    static {
        for (TradeStatus type : TradeStatus.values()) {
            map.put(type.value, type);
        }
    }

    TradeStatus(int value) {
        this.value = value;
    }

    @JsonValue
    public int getValue() {
        return value;
    }

    public static TradeStatus valueOf(int value) {
        return map.get(Integer.valueOf(value));
    }
}