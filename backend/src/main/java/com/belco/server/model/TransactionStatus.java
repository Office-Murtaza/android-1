package com.belco.server.model;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.HashMap;
import java.util.Map;

public enum TransactionStatus {

    PENDING(1),
    COMPLETE(2),
    FAIL(3),
    NOT_EXIST(4);

    private int value;

    private static final Map<Integer, TransactionStatus> map = new HashMap<>();

    static {
        for (TransactionStatus type : TransactionStatus.values()) {
            map.put(type.value, type);
        }
    }

    TransactionStatus(int value) {
        this.value = value;
    }

    @JsonValue
    public int getValue() {
        return value;
    }

    public static TransactionStatus valueOf(Integer value) {
        return map.get(value);
    }

    public int getConfirmations() {
        if (this == COMPLETE) {
            return 3;
        }

        return 0;
    }
}