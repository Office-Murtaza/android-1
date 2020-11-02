package com.belco.server.util;

import com.belco.server.entity.User;
import net.sf.json.JSONObject;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Calendar;
import java.util.Date;

public class Util {

    public static BigDecimal convert(String str) {
        return new BigDecimal(str);
    }

    public static String convert(BigDecimal value) {
        if (value != null) {
            return value.toString();
        }

        return null;
    }

    public static <T> T nvl(T var1, T var2) {
        if (var1 == null) {
            return var2;
        }

        return var1;
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

    public static BigDecimal format(BigDecimal value, int scale) {
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

    public static void uploadFile(MultipartFile file, Path path) throws IOException {
        Files.write(path, file.getBytes());
    }

    public static String sign(String method, String coin, long timestamp, String apiKey, String apiSecret) {
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

    public static void reverse(byte[] array) {
        for (int i = 0; i < array.length / 2; i++) {
            byte temp = array[i];
            array[i] = array[array.length - i - 1];
            array[array.length - i - 1] = temp;
        }
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