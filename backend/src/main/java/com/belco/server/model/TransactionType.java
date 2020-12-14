package com.belco.server.model;

import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

public enum TransactionType {

    DEPOSIT(1),
    WITHDRAW(2),
    TRANSFER_SEND(3),
    TRANSFER_RECEIVE(4),
    BUY(5),
    SELL(6),
    MOVE(7),
    SWAP_SEND(8),
    SWAP_RECEIVE(9),
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
        } else if (type2 == TRANSFER_SEND || type2 == TRANSFER_RECEIVE) {
            if (type == WITHDRAW) {
                return TRANSFER_SEND;
            } else if (type == DEPOSIT) {
                return TRANSFER_RECEIVE;
            }
        } else if (type2 == SWAP_RECEIVE || type2 == SWAP_SEND) {
            if (type == WITHDRAW) {
                return SWAP_SEND;
            } else if (type == DEPOSIT) {
                return SWAP_RECEIVE;
            }
        }

        return type2;
    }

    @JsonValue
    public int getValue() {
        return value;
    }
}