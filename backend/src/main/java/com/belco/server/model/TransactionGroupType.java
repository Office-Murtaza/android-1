package com.belco.server.model;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.HashMap;
import java.util.Map;

public enum TransactionGroupType {

    GIFT(1),
    EXCHANGE(2);

    private int value;

    private static final Map<Integer, TransactionGroupType> map = new HashMap<>();

    static {
        for (TransactionGroupType type : TransactionGroupType.values()) {
            map.put(type.value, type);
        }
    }

    TransactionGroupType(int value) {
        this.value = value;
    }

    @JsonValue
    public int getValue() {
        return value;
    }

    public static TransactionGroupType valueOf(Integer value) {
        return map.get(value);
    }
}