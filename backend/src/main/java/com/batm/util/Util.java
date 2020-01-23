package com.batm.util;

import com.batm.dto.TransactionDTO;
import com.batm.dto.TransactionListDTO;
import com.batm.entity.TransactionRecord;
import com.batm.entity.TransactionRecordGift;
import com.batm.model.TransactionStatus;
import com.batm.model.TransactionType;
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
import java.util.*;

public class Util {

    public static BigDecimal convert(String str) {
        return new BigDecimal(str).setScale(2, RoundingMode.DOWN);
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

    public static TransactionListDTO buildTxs(Map<String, TransactionDTO> map, Integer startIndex, Integer limit, List<TransactionRecordGift> giftList, List<TransactionRecord> txList) {
        Util.mergeGifts(map, giftList);
        Util.mergeTxs(map, txList);
        List<TransactionDTO> list = Util.convertAndSort(map);

        return Util.build(list, startIndex, limit);
    }

    private static void mergeGifts(Map<String, TransactionDTO> map, List<TransactionRecordGift> giftList) {
        if (giftList != null && !giftList.isEmpty()) {
            giftList.stream().forEach(e -> {
                if (map.containsKey(e.getTxId())) {
                    TransactionType type = map.get(e.getTxId()).getType();
                    map.get(e.getTxId()).setType(TransactionType.getGiftType(type));
                }
            });
        }
    }

    private static void mergeTxs(Map<String, TransactionDTO> map, List<TransactionRecord> txList) {
        if (txList != null && !txList.isEmpty()) {
            txList.stream().forEach(e -> {
                TransactionType type = e.getTransactionType();
                TransactionStatus status = e.getTransactionStatus(type);

                if (StringUtils.isNotEmpty(e.getDetail()) && map.containsKey(e.getDetail())) {
                    map.get(e.getDetail()).setType(type);
                    map.get(e.getDetail()).setStatus(status);
                } else {
                    TransactionDTO transactionDTO = new TransactionDTO(
                            e.getDetail(),
                            Util.format6(e.getCryptoAmount()),
                            type,
                            status,
                            e.getServerTime());
                    transactionDTO.setTxDbId(e.getId().toString());
                    map.put(e.getDetail(), transactionDTO);
                }
            });
        }
    }

    private static List<TransactionDTO> convertAndSort(Map<String, TransactionDTO> map) {
        if (!map.isEmpty()) {
            List<TransactionDTO> list = new ArrayList<>(map.values());
            list.sort(Comparator.comparing(TransactionDTO::getDate1).reversed());

            return list;
        }

        return new ArrayList<>();
    }

    private static TransactionListDTO build(List<TransactionDTO> list, Integer startIndex, Integer limit) {
        TransactionListDTO result = new TransactionListDTO();
        List<TransactionDTO> transactions = new ArrayList<>();

        for (int i = 0; i < list.size(); i++) {
            if ((i + 1 < startIndex)) {
                continue;
            }

            TransactionDTO dto = list.get(i);
            dto.setIndex(startIndex + i);
            transactions.add(dto);

            if ((startIndex + limit) == (i + 1)) {
                break;
            }
        }

        result.setTotal(list.size());
        result.setTransactions(transactions);

        return result;
    }

    public static String formatPhone(String phone) {
        return phone.substring(0, 2) + " " + phone.substring(2, 5) + "-" + phone.substring(5, 8) + "-" + phone.substring(8, 12);
    }

//    public static String addLeadingZeroes(String str) {
//        String res = "";
//
//        if (str.length() < 64) {
//            int i = 0;
//            while ((64 - str.length()) > i) {
//                i++;
//                res += "0";
//            }
//
//            return res + str;
//        }
//
//        return str;
//    }
}