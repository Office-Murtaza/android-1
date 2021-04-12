package com.belco.server.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.HashMap;
import java.util.Map;

public enum ProcessedType {

    PENDING(1),
    COMPLETE(2),
    FAIL(3),
    INSUFFICIENT_BALANCE(4);

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