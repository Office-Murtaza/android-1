package com.batm.model;

import com.fasterxml.jackson.annotation.JsonValue;

public enum TradeStatus {

    CREATED(1),
    PAID(2),
    RELEASED(3),
    COMPLETE(4),
    CANCELED(5),
    DISPUTED(6),
    EXPIRED(7);

    private int value;

    TradeStatus(int value) {
        this.value = value;
    }

    @JsonValue
    public int getValue() {
        return value;
    }
}