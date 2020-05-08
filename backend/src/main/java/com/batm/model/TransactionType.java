package com.batm.model;

import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

public enum TransactionType {

    UNKNOWN(0),
    DEPOSIT(1),
    WITHDRAW(2),
    SEND_GIFT(3),
    RECEIVE_GIFT(4),
    BUY(5),
    SELL(6),
    MOVE(7),
    SEND_EXCHANGE(8),
    RECEIVE_EXCHANGE(9),
    RESERVE(10),
    RECALL(11);

    private int value;

    TransactionType(int value) {
        this.value = value;
    }

    @JsonValue
    public int getValue() {
        return value;
    }

    public static TransactionType getType(String fromAddress, String toAddress, String address) {
        if (StringUtils.isNotBlank(address)) {
            if (address.equalsIgnoreCase(fromAddress)) {
                return TransactionType.WITHDRAW;
            } else if (address.equalsIgnoreCase(toAddress)) {
                return TransactionType.DEPOSIT;
            }
        }

        return TransactionType.UNKNOWN;
    }

    public static TransactionType convert(TransactionType type, TransactionGroupType group) {
        if (type != null) {
            if (type == WITHDRAW) {
                if (group == TransactionGroupType.GIFT) {
                    return SEND_GIFT;
                } else if (group == TransactionGroupType.C2C) {
                    return SEND_EXCHANGE;
                }
            } else if (type == DEPOSIT) {
                if (group == TransactionGroupType.GIFT) {
                    return RECEIVE_GIFT;
                } else if (group == TransactionGroupType.C2C) {
                    return RECEIVE_EXCHANGE;
                }
            }
        }

        return TransactionType.UNKNOWN;
    }
}