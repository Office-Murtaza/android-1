package com.belco.server.model;

import com.fasterxml.jackson.annotation.JsonValue;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

public enum PricePeriod {

    DAY(1) {
        @Override
        public long getFrom() {
            return Instant.now().minus(1, ChronoUnit.DAYS).getEpochSecond();
        }
    },
    WEEK(2) {
        @Override
        public long getFrom() {
            return Instant.now().minus(1, ChronoUnit.WEEKS).getEpochSecond();
        }
    },
    MONTH(3) {
        @Override
        public long getFrom() {
            return Instant.now().minus(1, ChronoUnit.MONTHS).getEpochSecond();
        }
    },
    MONTH_3(4) {
        @Override
        public long getFrom() {
            return Instant.now().minus(3, ChronoUnit.MONTHS).getEpochSecond();
        }
    },
    YEAR(5) {
        @Override
        public long getFrom() {
            return Instant.now().minus(1, ChronoUnit.YEARS).getEpochSecond();
        }
    };

    private int value;

    public abstract long getFrom();

    private static final Map<Integer, PricePeriod> map = new HashMap<>();

    static {
        for (PricePeriod type : PricePeriod.values()) {
            map.put(type.value, type);
        }
    }

    PricePeriod(int value) {
        this.value = value;
    }

    @JsonValue
    public int getValue() {
        return value;
    }

    public static PricePeriod valueOf(Integer value) {
        return map.get(value);
    }
}