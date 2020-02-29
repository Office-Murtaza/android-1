package com.batm.model;

import com.fasterxml.jackson.annotation.JsonValue;

public enum VerificationStatus {

    NOT_VERIFIED(1),
    PENDING(2),
    REJECTED(3),
    ACCEPTED(4),
    VIP_PENDING(5),
    VIP_REJECTED(6),
    VIP_ACCEPTED(7);

    private int value;

    VerificationStatus(int value) {
        this.value = value;
    }

    @JsonValue
    public int getValue() {
        return value;
    }
}