package com.belco.server.model;

import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

public enum TransactionType {

    DEPOSIT(1),
    WITHDRAW(2),
    SEND_TRANSFER(3),
    RECEIVE_TRANSFER(4),
    BUY(5),
    SELL(6),
    MOVE(7),
    SEND_SWAP(8),
    RECEIVE_SWAP(9),
    RESERVE(10),
    RECALL(11),
    SELF(12),
    CREATE_STAKE(13),
    CANCEL_STAKE(14),
    WITHDRAW_STAKE(15);

    private static final Map<Integer, TransactionType> map = new HashMap<>();

    static {
        for (TransactionType type : TransactionType.values()) {
            map.put(type.value, type);
        }
    }

    private int value;

    TransactionType(int value) {
        this.value = value;
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
        if (type == TransactionType.SELF) {
            return type;
        } else if (type2 == SEND_TRANSFER || type2 == RECEIVE_TRANSFER) {
            if (type == WITHDRAW) {
                return SEND_TRANSFER;
            } else if (type == DEPOSIT) {
                return RECEIVE_TRANSFER;
            }
        } else if (type2 == RECEIVE_SWAP || type2 == SEND_SWAP) {
            if (type == WITHDRAW) {
                return SEND_SWAP;
            } else if (type == DEPOSIT) {
                return RECEIVE_SWAP;
            }
        }

        return type2;
    }

    @JsonValue
    public int getValue() {
        return value;
    }
}