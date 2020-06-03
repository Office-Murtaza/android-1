package com.batm.util;

import com.batm.entity.User;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.RandomStringUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.Optional;

public class Util {

    public static BigDecimal convert(String str) {
        return new BigDecimal(str).setScale(3, RoundingMode.DOWN).stripTrailingZeros();
    }

    public static String generatePublicId() {
        return "I" + RandomStringUtils.randomAlphanumeric(15).toUpperCase();
    }

    public static String createRefreshToken() {
        return RandomStringUtils.randomAlphanumeric(250);
    }

    public static Date getStartDate() {
        Calendar c = Calendar.getInstance();

        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        return c.getTime();
    }

    public static BigDecimal format2(BigDecimal value) {
        return format(value, 2);
    }

    public static BigDecimal format6(BigDecimal value) {
        return format(value, 6);
    }

    private static BigDecimal format(BigDecimal value, int scale) {
        if (value == null) {
            return null;
        }

        return value.setScale(scale, RoundingMode.DOWN).stripTrailingZeros();
    }

    public static JSONObject insecureRequest(String path) {
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return null;
                        }

                        public void checkClientTrusted(
                                java.security.cert.X509Certificate[] certs, String authType) {
                        }

                        public void checkServerTrusted(
                                java.security.cert.X509Certificate[] certs, String authType) {
                        }
                    }
            };

            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

            URL url = new URL(path);
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            conn.setHostnameVerifier((arg0, arg1) -> true);

            BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
            StringBuilder sb = new StringBuilder();
            String output;

            while ((output = br.readLine()) != null) {
                sb.append(output);
            }

            conn.disconnect();

            return JSONObject.fromObject(sb.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static String formatPhone(String phone) {
        int length = phone.length();

        return phone.substring(0, length - 10) + " " + phone.substring(length - 10, length - 7) + "-" + phone.substring(length - 7, length - 4) + "-" + phone.substring(length - 4, length);
    }

    //TODO: rewrite
    public static String getExtensionByStringHandling(String filename) {
        return Optional.ofNullable(filename)
                .filter(f -> f.contains("."))
                .map(f -> f.substring(filename.lastIndexOf(".")))
                .orElse(StringUtils.EMPTY);
    }

    public static String sign(String method, String coin, long timestamp, String apiKey, String apiSecret) {//(String message, String secret) {
        try {
            String message = method + "," + apiKey + "," + coin + "," + timestamp;

            SecretKeySpec key = new SecretKeySpec(apiSecret.getBytes(), "HmacSHA256");
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(key);

            byte[] bytes = mac.doFinal(message.getBytes("ASCII"));

            StringBuffer hash = new StringBuffer();

            for (int i = 0; i < bytes.length; i++) {
                String hex = Integer.toHexString(0xFF & bytes[i]);
                if (hex.length() == 1) {
                    hash.append('0');
                }
                hash.append(hex);
            }
            return hash.toString();
        } catch (Exception e) {
            throw new RuntimeException("Unable to sign message", e);
        }
    }

    public static int distance(User user, User user2) {
        return distance(user.getLatitude().doubleValue(), user.getLongitude().doubleValue(), user2.getLatitude().doubleValue(), user2.getLongitude().doubleValue());
    }

    public static int distance(double lat1, double lon1, double lat2, double lon2) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515 * 1.609344;

        return (int) Math.ceil(dist);
    }

    private static double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private static double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }

    public static String getPriceDayColl(String coin) {
        return "price_day_" + coin;
    }

    public static String getPriceWeekColl(String coin) {
        return "price_week_" + coin;
    }

    public static String getPriceMonthColl(String coin) {
        return "price_month_" + coin;
    }

    public static String getPrice3MonthColl(String coin) {
        return "price_three_months_" + coin;
    }

    public static String getPriceYearColl(String coin) {
        return "price_year_" + coin;
    }
}