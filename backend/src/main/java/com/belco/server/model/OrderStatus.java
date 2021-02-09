package com.belco.server.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.HashMap;
import java.util.Map;

public enum OrderStatus {

    NEW(1),
    CANCELED(2),
    DOING(3),
    PAID(4),
    RELEASED(5),
    DISPUTING(6),
    SOLVED(7);

    private static final Map<Integer, OrderStatus> map = new HashMap<>();

    static {
        for (OrderStatus type : OrderStatus.values()) {
            map.put(type.value, type);
        }
    }

    private int value;

    OrderStatus(int value) {
        this.value = value;
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static OrderStatus valueOf(Integer value) {
        return map.get(value);
    }

    @JsonValue
    public int getValue() {
        return value;
    }
}