package com.batm.model;

import com.fasterxml.jackson.annotation.JsonValue;

public enum TransactionGroupType {

    UNKNOWN(0),
    GIFT(1),
    C2C(2);

    private int value;

    TransactionGroupType(int value) {
        this.value = value;
    }

    @JsonValue
    public int getValue() {
        return value;
    }
}