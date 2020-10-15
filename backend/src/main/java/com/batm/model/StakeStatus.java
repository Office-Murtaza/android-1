package com.batm.model;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.HashMap;
import java.util.Map;

public enum StakeStatus {

    NOT_EXIST(1),
    CREATE_PENDING(2),
    CREATED(3),
    CANCEL_PENDING(4),
    CANCELED(5),
    WITHDRAW_PENDING(6),
    WITHDRAWN(7);

    private int value;

    private static final Map<Integer, StakeStatus> map = new HashMap<>();

    static {
        for (StakeStatus type : StakeStatus.values()) {
            map.put(type.value, type);
        }
    }

    StakeStatus(int value) {
        this.value = value;
    }

    @JsonValue
    public int getValue() {
        return value;
    }

    public static StakeStatus valueOf(Integer value) {
        return map.get(value);
    }

    public static StakeStatus convert(TransactionType type, TransactionStatus status) {
        if (type == TransactionType.CREATE_STAKE) {
            if (status == TransactionStatus.PENDING) {
                return CREATE_PENDING;
            } else if (status == TransactionStatus.COMPLETE) {
                return CREATED;
            } else {
                return NOT_EXIST;
            }
        } else if (type == TransactionType.CANCEL_STAKE) {
            if (status == TransactionStatus.PENDING) {
                return CANCEL_PENDING;
            } else if (status == TransactionStatus.COMPLETE) {
                return CANCELED;
            } else {
                return CREATED;
            }
        } else if (type == TransactionType.WITHDRAW_STAKE) {
            if (status == TransactionStatus.PENDING) {
                return WITHDRAW_PENDING;
            } else if (status == TransactionStatus.COMPLETE) {
                return WITHDRAWN;
            } else {
                return CANCELED;
            }
        }

        return NOT_EXIST;
    }
}