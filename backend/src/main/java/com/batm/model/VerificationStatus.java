package com.batm.model;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.HashMap;
import java.util.Map;

public enum VerificationStatus {

    NOT_VERIFIED(1),
    VERIFICATION_PENDING(2),
    VERIFICATION_REJECTED(3),
    VERIFIED(4),
    VIP_VERIFICATION_PENDING(5),
    VIP_VERIFICATION_REJECTED(6),
    VIP_VERIFIED(7);

    private int value;

    private static final Map<Integer, VerificationStatus> map = new HashMap<>();

    static {
        for (VerificationStatus type : VerificationStatus.values()) {
            map.put(type.value, type);
        }
    }

    VerificationStatus(int value) {
        this.value = value;
    }

    @JsonValue
    public int getValue() {
        return value;
    }

    public static VerificationStatus valueOf(int value) {
        return map.get(Integer.valueOf(value));
    }
}