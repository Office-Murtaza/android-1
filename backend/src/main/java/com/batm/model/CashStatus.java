package com.batm.model;

import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.BooleanUtils;

public enum CashStatus {

    NOT_AVAILABLE(0),
    AVAILABLE(1),
    WITHDRAWN(2);

    private int value;

    CashStatus(int value) {
        this.value = value;
    }

    @JsonValue
    public int getValue() {
        return value;
    }

    public static CashStatus getCashStatus(Boolean canBeAllocatedForWithdrawal, Boolean isWithdrawn) {
        if (BooleanUtils.isTrue(canBeAllocatedForWithdrawal)) {
            if (BooleanUtils.isTrue(isWithdrawn)) {
                return CashStatus.WITHDRAWN;
            }
            return CashStatus.AVAILABLE;
        } else {
            return CashStatus.NOT_AVAILABLE;
        }
    }
}
