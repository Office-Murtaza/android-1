package com.belco.server.model;

import java.util.HashMap;
import java.util.Map;

public enum StakingStatus {

    NOT_EXIST(1),
    CREATE_PENDING(2),
    CREATED(3),
    CANCEL_PENDING(4),
    CANCELED(5),
    WITHDRAW_PENDING(6),
    WITHDRAWN(7);

    private static final Map<Integer, StakingStatus> map = new HashMap<>();

    static {
        for (StakingStatus type : StakingStatus.values()) {
            map.put(type.value, type);
        }
    }

    private int value;

    StakingStatus(int value) {
        this.value = value;
    }

    public static StakingStatus valueOf(Integer value) {
        return map.get(value);
    }

    public static StakingStatus convert(TransactionType type, TransactionStatus status) {
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