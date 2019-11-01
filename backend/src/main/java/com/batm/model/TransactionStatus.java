package com.batm.model;

import com.fasterxml.jackson.annotation.JsonValue;

public enum TransactionStatus {

    PENDING(1),
    COMPLETE(2),
    FAIL(3);

    private int value;

    TransactionStatus(int value) {
        this.value = value;
    }

    @JsonValue
    public int getValue() {
        return value;
    }
}