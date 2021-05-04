package com.belco.server.util;

import net.sf.json.JSONObject;
import org.apache.commons.lang3.RandomStringUtils;

import javax.crypto.Cipher;
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
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;
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

    public static Date getStartOfDay() {
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

    public static void reverse(byte[] array) {
        for (int i = 0; i < array.length / 2; i++) {
            byte temp = array[i];
            array[i] = array[array.length - i - 1];
            array[array.length - i - 1] = temp;
        }
    }

    private static SecretKeySpec getKey(String secret) {
        try {
            byte[] key = secret.getBytes("UTF-8");
            MessageDigest sha = MessageDigest.getInstance("SHA-1");
            key = sha.digest(key);
            key = Arrays.copyOf(key, 16);

            return new SecretKeySpec(key, "AES");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static String encrypt(String strToEncrypt, String secret) {
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, getKey(secret));

            return Base64.getEncoder().encodeToString(cipher.doFinal(strToEncrypt.getBytes("UTF-8")));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static String decrypt(String strToDecrypt, String secret) {
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, getKey(secret));

            return new String(cipher.doFinal(Base64.getDecoder().decode(strToDecrypt)));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}