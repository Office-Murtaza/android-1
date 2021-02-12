package com.belco.server.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.HashMap;
import java.util.Map;

public enum VerificationTier {

    VERIFICATION(1),
    VIP_VERIFICATION(2);

    private static final Map<Integer, VerificationTier> map = new HashMap<>();

    static {
        for (VerificationTier type : VerificationTier.values()) {
            map.put(type.value, type);
        }
    }

    private int value;

    VerificationTier(int value) {
        this.value = value;
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static VerificationTier valueOf(Integer value) {
        return map.get(value);
    }

    @JsonValue
    public int getValue() {
        return value;
    }
}