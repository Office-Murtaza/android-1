package com.batm.model;

import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang.StringUtils;

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

    public static TransactionType getType(String fromAddress, String toAddress, String address) {
        if (StringUtils.isNotEmpty(fromAddress) && fromAddress.equalsIgnoreCase(address)) {
            return WITHDRAW;
        } else if (StringUtils.isNotEmpty(toAddress) && toAddress.equalsIgnoreCase(address)) {
            return DEPOSIT;
        }

        return null;
    }

    public static TransactionType getGiftType(TransactionType type) {
        if (type == WITHDRAW) {
            return SEND_GIFT;
        } else if(type == DEPOSIT) {
            return RECEIVE_GIFT;
        } else {
            return type;
        }
    }
}