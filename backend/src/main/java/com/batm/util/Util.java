package com.batm.util;

import net.sf.json.JSONArray;
import org.apache.commons.lang3.RandomStringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class Util {

    public static BigDecimal convert(String str) {
        return new BigDecimal(str).setScale(2, RoundingMode.DOWN);
    }

    public static <T> List<T> jsonArrayToList(JSONArray jsonArray) {
        List<T> list = new ArrayList<>();
        if (jsonArray != null) {
            int len = jsonArray.size();
            for (int i = 0; i < len; i++) {
                list.add((T) jsonArray.opt(i));
            }
        }
        return list;
    }

    public static String generatePublicId() {
        return "I" + RandomStringUtils.randomAlphanumeric(15).toUpperCase();
    }

    public static String createRefreshToken() {
        return RandomStringUtils.randomAlphanumeric(250);
    }
}