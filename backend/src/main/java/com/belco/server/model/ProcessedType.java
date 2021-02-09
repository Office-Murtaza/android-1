package com.belco.server.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.HashMap;
import java.util.Map;

public enum ProcessedType {

    SUCCESS(0),
    INSUFFICIENT_WALLET_BALANCE(1),
    ERROR_CREATE_TRANSACTION(2);

    private static final Map<Integer, ProcessedType> map = new HashMap<>();

    static {
        for (ProcessedType type : ProcessedType.values()) {
            map.put(type.value, type);
        }
    }

    private int value;

    ProcessedType(int value) {
        this.value = value;
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static ProcessedType valueOf(Integer value) {
        return map.get(value);
    }

    @JsonValue
    public int getValue() {
        return value;
    }
}