package com.batm.model;

import com.fasterxml.jackson.annotation.JsonValue;

public enum TransactionType {

    DEPOSIT(1),
    WITHDRAW(2),
    SEND_GIFT(3),
    RECEIVE_GIFT(4),
    BUY(5),
    SELL(6);

    private int value;

    TransactionType(int value) {
        this.value = value;
    }

    @JsonValue
    public int getValue() {
        return value;
    }
}