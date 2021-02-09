package com.belco.server.model;

import com.fasterxml.jackson.annotation.JsonCreator;
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
            return Instant.now().minus(7, ChronoUnit.DAYS).getEpochSecond();
        }
    },
    MONTH(3) {
        @Override
        public long getFrom() {
            return Instant.now().minus(30, ChronoUnit.DAYS).getEpochSecond();
        }
    },
    MONTH_3(4) {
        @Override
        public long getFrom() {
            return Instant.now().minus(90, ChronoUnit.DAYS).getEpochSecond();
        }
    },
    YEAR(5) {
        @Override
        public long getFrom() {
            return Instant.now().minus(365, ChronoUnit.DAYS).getEpochSecond();
        }
    };

    private static final Map<Integer, PricePeriod> map = new HashMap<>();

    static {
        for (PricePeriod type : PricePeriod.values()) {
            map.put(type.value, type);
        }
    }

    private int value;

    PricePeriod(int value) {
        this.value = value;
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static PricePeriod valueOf(Integer value) {
        return map.get(value);
    }

    public abstract long getFrom();

    @JsonValue
    public int getValue() {
        return value;
    }
}