package com.batm.service;

import com.batm.dto.CurrentBlockDTO;
import com.batm.dto.SubmitTransactionDTO;
import com.batm.dto.TransactionResponseDTO;
import com.batm.model.TransactionStatus;
import com.batm.util.Constant;
import com.batm.util.TransactionUtil;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class TrongridService {

    @Autowired
    private RestTemplate rest;

    @Value("${trx.url}")
    private String url;

    public BigDecimal getBalance(String address) {
        try {
            JSONObject res = rest.getForObject(url + "/v1/accounts/" + address, JSONObject.class);
            JSONArray data = res.getJSONArray("data");

            if (!data.isEmpty()) {
                return new BigDecimal(data.getJSONObject(0).getString("balance"))
                        .divide(BigDecimal.valueOf(Constant.TRX_DIVIDER))
                        .setScale(5, RoundingMode.DOWN);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return BigDecimal.ZERO;
    }

    public String submitTransaction(SubmitTransactionDTO transaction) {
        try {
            JSONObject res = JSONObject.fromObject(rest.postForObject(url + "/wallet/broadcasttransaction", transaction.getTrx(), String.class));

            if(res.optBoolean("result")) {
                return transaction.getTrx().optString("txID");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public TransactionStatus getTransactionStatus(String txId) {
        try {
            JSONObject res = rest.getForObject(url + "/v1/transactions/" + txId, JSONObject.class);
            JSONArray array = res.optJSONArray("data");

            if (array != null && !array.isEmpty()) {
                if (array.getJSONObject(0).optJSONArray("ret").getJSONObject(0).optString("contractRet").equalsIgnoreCase("SUCCESS")) {
                    return TransactionStatus.COMPLETE;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return TransactionStatus.PENDING;
    }

    public TransactionResponseDTO getTransactions(String address, Integer startIndex, Integer limit) {
        try {
            JSONObject res = rest.getForObject(url + "/v1/accounts/" + address + "/transactions?limit=200", JSONObject.class);
            JSONArray array = res.optJSONArray("data");

            if (array != null && !array.isEmpty()) {
                return TransactionUtil.composeTrongrid(array, address, Constant.TRX_DIVIDER, startIndex, limit);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new TransactionResponseDTO();
    }

    public CurrentBlockDTO getCurrentBlock() {
        try {
            String resStr = rest.getForObject(url + "/wallet/getnowblock", String.class);
            JSONObject res = JSONObject.fromObject(resStr);

            return new CurrentBlockDTO(res.optJSONObject("block_header"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new CurrentBlockDTO();
    }
}