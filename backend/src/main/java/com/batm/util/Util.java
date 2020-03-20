package com.batm.util;

import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.RandomStringUtils;

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
        return new BigDecimal(str).setScale(3, RoundingMode.UP);
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

    /**
     * Returns file extension with dot or empty string.
     * @param filename
     * @return
     */
    public static String getExtensionByStringHandling(String filename) {
        return Optional.ofNullable(filename)
                .filter(f -> f.contains("."))
                .map(f -> f.substring(filename.indexOf(".") + 1))
                .orElse(StringUtils.EMPTY);
    }
}