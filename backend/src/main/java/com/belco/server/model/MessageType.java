package com.belco.server.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.HashMap;
import java.util.Map;

public enum MessageType {

    SMS(1),
    PUSH(2);

    private static final Map<Integer, MessageType> map = new HashMap<>();

    static {
        for (MessageType type : MessageType.values()) {
            map.put(type.value, type);
        }
    }

    private int value;

    MessageType(int value) {
        this.value = value;
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static MessageType valueOf(Integer value) {
        return map.get(value);
    }

    @JsonValue
    public int getValue() {
        return value;
    }
}
