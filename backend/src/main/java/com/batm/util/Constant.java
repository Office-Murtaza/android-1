package com.batm.util;

import java.math.BigDecimal;

public class Constant {

    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String AUTHORITIES_KEY = "auth";

    public static final String REGEX_PHONE = "^(\\+1)[0-9]{10}$";
    public static final String DEFAULT_CODE = "1234";

    public static final int PASSWORD_MIN_LENGTH = 6;
    public static final int PASSWORD_MAX_LENGTH = 15;

    public static final BigDecimal BTC_DIVIDER = BigDecimal.valueOf(100000000L);
    public static final BigDecimal ETH_DIVIDER = BigDecimal.valueOf(1000000000000000000L);
    public static final BigDecimal BCH_DIVIDER = BigDecimal.valueOf(100000000L);
    public static final BigDecimal LTC_DIVIDER = BigDecimal.valueOf(100000000L);
    public static final BigDecimal XRP_DIVIDER = BigDecimal.valueOf(1000000L);
    public static final BigDecimal TRX_DIVIDER = BigDecimal.valueOf(1000000L);
    public static final BigDecimal BNB_DIVIDER = BigDecimal.valueOf(100000000L);

    public static final int TRANSACTION_LIMIT = 10;

    public static final int GIFT_USER_EXIST = 0;
    public static final int GIFT_USER_NOT_EXIST = 1;
    public static final int GIFT_USER_TRANSACTION_CREATED = 2;

    public static final BigDecimal DAILY_LIMIT = BigDecimal.valueOf(10000);
    public static final BigDecimal TX_LIMIT = BigDecimal.valueOf(3000);

    public static final String TERMINAL_SERIAL_NUMBER = "BT300197";
}