package com.batm.model;

import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum TransactionType {

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
    RECALL(11),
    SELF(12),
    STAKE(13),
    UNSTAKE(14);

    private int value;

    private static final Map<Integer, TransactionType> map = new HashMap<>();

    static {
        for (TransactionType type : TransactionType.values()) {
            map.put(type.value, type);
        }
    }

    TransactionType(int value) {
        this.value = value;
    }

    @JsonValue
    public int getValue() {
        return value;
    }

    public static TransactionType valueOf(Integer value) {
        return map.get(value);
    }

    public static TransactionType getType(String fromAddress, String toAddress, String address) {
        if (StringUtils.isNotBlank(fromAddress) && StringUtils.isNotBlank(toAddress) && fromAddress.equalsIgnoreCase(toAddress)) {
            return TransactionType.SELF;
        } else if (StringUtils.isNotBlank(address)) {
            if (address.equalsIgnoreCase(fromAddress)) {
                return TransactionType.WITHDRAW;
            } else if (address.equalsIgnoreCase(toAddress)) {
                return TransactionType.DEPOSIT;
            }
        }

        return null;
    }

    public static TransactionType convert(TransactionType type, TransactionType type2) {
        if (type2 == SEND_GIFT || type2 == RECEIVE_GIFT) {
            if (type == WITHDRAW) {
                return SEND_GIFT;
            } else if (type == DEPOSIT) {
                return RECEIVE_GIFT;
            }
        } else if (type2 == SEND_EXCHANGE || type2 == RECEIVE_EXCHANGE) {
            if (type == WITHDRAW) {
                return SEND_EXCHANGE;
            } else if (type == DEPOSIT) {
                return RECEIVE_EXCHANGE;
            }
        }

        return type2;
    }
}