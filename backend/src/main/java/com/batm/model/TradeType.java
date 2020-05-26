package com.batm.model;

import com.fasterxml.jackson.annotation.JsonValue;

public enum TradeType {

    BUY(1),
    SELL(2);

    private int value;

    TradeType(int value) {
        this.value = value;
    }

    @JsonValue
    public int getValue() {
        return value;
    }

    public static TradeType getRequestType(TradeType type) {
        if (type == BUY) {
            return SELL;
        }

        return BUY;
    }
}