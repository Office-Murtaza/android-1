package com.batm.model;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.HashMap;
import java.util.Map;

public enum TradeSort {

    PRICE(1),
    DISTANCE(2);

    private int value;

    private static final Map<Integer, TradeSort> map = new HashMap<>();

    static {
        for (TradeSort type : TradeSort.values()) {
            map.put(type.value, type);
        }
    }

    TradeSort(int value) {
        this.value = value;
    }

    @JsonValue
    public int getValue() {
        return value;
    }

    public static TradeSort valueOf(Integer value) {
        return map.get(value);
    }
}