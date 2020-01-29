package com.batm.util;

import java.math.BigDecimal;

public class Constant {

    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String AUTHORITIES_KEY = "auth";

    public static final String REGEX_PHONE = "^\\+(9[976]\\d|8[987530]\\d|6[987]\\d|5[90]\\d|42\\d|3[875]\\d|\n" +
            "2[98654321]\\d|9[8543210]|8[6421]|6[6543210]|5[87654321]|\n" +
            "4[987654310]|3[9643210]|2[70]|7|1)\\d{1,14}$";
    public static final String DEFAULT_CODE = "1234";

    public static final int PASSWORD_MIN_LENGTH = 6;
    public static final int PASSWORD_MAX_LENGTH = 15;

    public static final BigDecimal BTC_DIVIDER = BigDecimal.valueOf(100_000_000L);
    public static final BigDecimal ETH_DIVIDER = BigDecimal.valueOf(1_000_000_000_000_000_000L);
    public static final BigDecimal BCH_DIVIDER = BigDecimal.valueOf(100_000_000L);
    public static final BigDecimal LTC_DIVIDER = BigDecimal.valueOf(100_000_000L);
    public static final BigDecimal XRP_DIVIDER = BigDecimal.valueOf(1_000_000L);
    public static final BigDecimal TRX_DIVIDER = BigDecimal.valueOf(1_000_000L);
    public static final BigDecimal BNB_DIVIDER = BigDecimal.valueOf(100_000_000L);

    public static final int TRANSACTION_LIMIT = 10;

    public static final int GIFT_USER_EXIST = 0;
    public static final int GIFT_USER_NOT_EXIST = 1;
    public static final int GIFT_USER_TRANSACTION_CREATED = 2;

    public static final int STATE_REGISTERED = 1;
    public static final int TYPE_CELLPHONE = 4;

    public static final BigDecimal DAILY_LIMIT = BigDecimal.valueOf(10_000);
    public static final BigDecimal TX_LIMIT = BigDecimal.valueOf(3_000);

    public static final String TERMINAL_SERIAL_NUMBER = "BT300197";

    public static final long GAS_PRICE = 50_000_000_000L;
    public static final long GAS_LIMIT = 50_000;

    public static final String BNB_CHAIN_ID = "Binance-Chain-Tigris";
}