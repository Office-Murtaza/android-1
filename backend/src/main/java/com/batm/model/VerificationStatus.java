package com.batm.model;

import com.fasterxml.jackson.annotation.JsonValue;

public enum VerificationStatus {

    NOT_VERIFIED(1),
    VERIFICATION_PENDING(2),
    VERIFICATION_REJECTED(3),
    VERIFIED(4),
    VIP_VERIFICATION_PENDING(5),
    VIP_VERIFICATION_REJECTED(6),
    VIP_VERIFIED(7);

    private int value;

    VerificationStatus(int value) {
        this.value = value;
    }

    @JsonValue
    public int getValue() {
        return value;
    }

    public static VerificationStatus getByValue(int value) {
        for(VerificationStatus e: VerificationStatus.values()) {
            if(e.value == value) {
                return e;
            }
        }
        return null;// not found
    }
}