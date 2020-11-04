package com.belco.server.model;

import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.BooleanUtils;

public enum CashStatus {

    NOT_AVAILABLE(1),
    AVAILABLE(2),
    WITHDRAWN(3);

    private int value;

    CashStatus(int value) {
        this.value = value;
    }

    @JsonValue
    public int getValue() {
        return value;
    }

    public static CashStatus getCashStatus(Boolean canBeCashedOut, Boolean isWithdrawn) {
        if (BooleanUtils.isTrue(canBeCashedOut)) {
            if (BooleanUtils.isTrue(isWithdrawn)) {
                return CashStatus.WITHDRAWN;
            }
            return CashStatus.AVAILABLE;
        } else {
            return CashStatus.NOT_AVAILABLE;
        }
    }
}