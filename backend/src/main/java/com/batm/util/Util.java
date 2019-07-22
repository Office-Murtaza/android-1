package com.batm.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Util {

    public static BigDecimal convert(String str) {
        return new BigDecimal(str).setScale(2, RoundingMode.DOWN);
    }
}
