package com.batm.model;

import com.fasterxml.jackson.annotation.JsonValue;

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

    public static CashStatus getCashStatus(boolean canBeAllocatedForWithdrawal, boolean isWithdrawn) {
        if (canBeAllocatedForWithdrawal) {
            if (isWithdrawn) {
                return CashStatus.WITHDRAWN;
            }
            return CashStatus.AVAILABLE;
        } else {
            return CashStatus.NOT_AVAILABLE;
        }
    }
}
