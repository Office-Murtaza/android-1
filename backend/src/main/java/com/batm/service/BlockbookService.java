package com.batm.service;

import com.batm.dto.*;
import com.batm.util.TransactionUtil;
import com.batm.util.Util;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.math.BigDecimal;

@Service
public class BlockbookService {

    @Autowired
    private RestTemplate rest;

    public BigDecimal getBalance(String url, String address, BigDecimal divider) {
        try {
            JSONObject res = rest.getForObject(url + "/api/v2/address/" + address + "?details=basic", JSONObject.class);

            return Util.format5(new BigDecimal(res.optString("balance")).divide(divider));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return BigDecimal.ZERO;
    }

    public String submitTransaction(String url, SubmitTransactionDTO transaction) {
        try {
            JSONObject res = rest.getForObject(url + "/api/v2/sendtx/" + transaction.getHex(), JSONObject.class);
            System.out.println(res);

            String txId = RandomStringUtils.randomAlphanumeric(50);

            return txId;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public UtxoDTO getUTXO(String url, String xpub) {
        try {
            JSONArray res = rest.getForObject(url + "/api/v2/utxo/" + xpub, JSONArray.class);

            return new UtxoDTO(res);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new UtxoDTO();
    }

    public NonceDTO getNonce(String url, String address) {
        try {
            JSONObject res = rest.getForObject(url + "/api/v2/address/" + address + "?details=txs&pageSize=1000&page=1", JSONObject.class);
            JSONArray transactionsArray = res.optJSONArray("transactions");

            if (transactionsArray != null) {
                return new NonceDTO(transactionsArray.size());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new NonceDTO();
    }

    public TransactionListDTO getTransactionList(String url, String address, BigDecimal divider, Integer startIndex, Integer limit) {
        try {
            JSONObject res = rest.getForObject(url + "/api/v2/address/" + address + "?details=txs&pageSize=1000&page=1", JSONObject.class);
            JSONArray array = res.optJSONArray("transactions");

            if (array != null && !array.isEmpty()) {
                return TransactionUtil.composeBlockbook(res.optInt("txs"), array, address, divider, startIndex, limit);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new TransactionListDTO();
    }

    public TransactionNumberDTO getTransactionNumber(String url, String address, BigDecimal amount, BigDecimal divider) {
        try {
            JSONObject res = rest.getForObject(url + "/api/v2/address/" + address + "?details=txs", JSONObject.class);
            for (Object jsonTransactions : res.getJSONArray("transactions")) {
                for (Object operationObject : ((JSONObject) jsonTransactions).getJSONArray("vout")) {
                    if (operationObject instanceof JSONObject) {
                        JSONObject operationJson = ((JSONObject) operationObject);
                        String value = operationJson.getString("value");
                        BigDecimal bigValue = new BigDecimal(value).divide(divider).stripTrailingZeros();
                        int n = operationJson.getInt("n");

                        if (bigValue.equals(amount.stripTrailingZeros())) {
                            String transactionId = ((JSONObject) jsonTransactions).getString("txid");
                            return new TransactionNumberDTO(transactionId, n);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}