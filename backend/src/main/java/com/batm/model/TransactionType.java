package com.batm.model;

import com.fasterxml.jackson.annotation.JsonValue;

public enum TransactionType {

    UNKNOWN(0),
    DEPOSIT(1),
    WITHDRAW(2),
    SEND_GIFT(3),
    RECEIVE_GIFT(4),
    BUY(5),
    SELL(6),
    MOVE(7),
    SEND_C2C(8),
    RECEIVE_C2C(9);

    private int value;

    TransactionType(int value) {
        this.value = value;
    }

    @JsonValue
    public int getValue() {
        return value;
    }

    public static TransactionType getType(String fromAddress, String toAddress, String address) {
        if (address.equalsIgnoreCase(fromAddress)) {
            return TransactionType.WITHDRAW;
        } else if (address.equalsIgnoreCase(toAddress)) {
            return TransactionType.DEPOSIT;
        } else {
            return TransactionType.UNKNOWN;
        }
    }

    public static TransactionType convert(TransactionType type, TransactionGroupType group) {
        if (type == WITHDRAW) {
            if (group == TransactionGroupType.GIFT) {
                return SEND_GIFT;
            } else if (group == TransactionGroupType.C2C) {
                return SEND_C2C;
            }
        } else if (type == DEPOSIT) {
            if (group == TransactionGroupType.GIFT) {
                return RECEIVE_GIFT;
            } else if (group == TransactionGroupType.C2C) {
                return RECEIVE_C2C;
            }
        }

        return TransactionType.UNKNOWN;
    }
}