package com.belco.server.model;

import com.fasterxml.jackson.annotation.JsonCreator;
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

    private static final Map<Integer, VerificationStatus> map = new HashMap<>();

    static {
        for (VerificationStatus type : VerificationStatus.values()) {
            map.put(type.value, type);
        }
    }

    private int value;

    VerificationStatus(int value) {
        this.value = value;
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static VerificationStatus valueOf(Integer value) {
        return map.get(value);
    }

    public static VerificationStatus convertToTradeVerificationStatus(VerificationStatus status) {
        if(status == VerificationStatus.VERIFICATION_PENDING || status == VerificationStatus.VERIFICATION_REJECTED) {
            return NOT_VERIFIED;
        } else if(status == VerificationStatus.VIP_VERIFICATION_PENDING || status == VerificationStatus.VIP_VERIFICATION_REJECTED) {
            return VERIFIED;
        } else {
            return status;
        }
    }

    @JsonValue
    public int getValue() {
        return value;
    }
}