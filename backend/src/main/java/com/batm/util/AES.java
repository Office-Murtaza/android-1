package com.batm.util;

import java.util.Arrays;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;

public class AES {

    private static SecretKeySpec secretKey;
    private static byte[] key;

    private static void setKey(String myKey) {
        MessageDigest sha = null;

        try {
            key = myKey.getBytes("UTF-8");
            sha = MessageDigest.getInstance("SHA-1");
            key = sha.digest(key);
            key = Arrays.copyOf(key, 16);
            secretKey = new SecretKeySpec(key, "AES");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String encrypt(String str, String secret) {
        try {
            setKey(secret);

            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);

            return Base64.getEncoder().encodeToString(cipher.doFinal(str.getBytes("UTF-8")));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static String decrypt(String str, String secret) {
        try {
            setKey(secret);
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);

            return new String(cipher.doFinal(Base64.getDecoder().decode(str)));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}