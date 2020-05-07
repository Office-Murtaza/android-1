package com.batm.model;

import com.fasterxml.jackson.annotation.JsonValue;

public enum TradeType {

    UNKNOWN(0),
    BUY(1),
    SELL(2),
    OPEN(3);

    private int value;

    TradeType(int value) {
        this.value = value;
    }

    @JsonValue
    public int getValue() {
        return value;
    }
}