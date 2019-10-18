package com.batm.util;

import java.math.BigDecimal;

public class Constant {

    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String AUTHORITIES_KEY = "auth";

    public static final String REGEX_PHONE = "^(\\+1)[0-9]{10}$";
    public static final String DEFAULT_CODE = "1234";

    public static final int PASSWORD_MIN_LENGTH = 6;
    public static final int PASSWORD_MAX_LENGTH = 15;

    public static final Long BTC_DIVIDER = 100000000L;
    public static final Long ETH_DIVIDER = 1000000000000000000L;
    public static final Long BCH_DIVIDER = 100000000L;
    public static final Long LTC_DIVIDER = 100000000L;
    public static final Long XRP_DIVIDER = 1000000L;
    public static final Long TRX_DIVIDER = 1000000L;

    public static final int TRANSACTION_LIMIT = 10;
    public static final int DISABLED = 0;
    public static final int ENABLED = 1;

    public static final int GIFT_USER_EXIST = 0;
    public static final int GIFT_USER_NOT_EXIST = 1;
    public static final int GIFT_USER_TRANSACTION_CREATED = 2;

    public static final BigDecimal DAILY_LIMIT = BigDecimal.valueOf(10000);
    public static final BigDecimal TX_LIMIT = BigDecimal.valueOf(3000);
}