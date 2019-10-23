package com.batm.service;

import com.batm.dto.CurrentAccountDTO;
import com.batm.dto.SubmitTransactionDTO;
import com.batm.dto.TransactionResponseDTO;
import com.batm.model.TransactionStatus;
import com.batm.util.Constant;
import com.batm.util.TransactionUtil;
import com.batm.util.Util;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.math.BigDecimal;

@Service
public class RippledService {

    @Autowired
    private RestTemplate rest;

    @Value("${xrp.url}")
    private String url;

    public BigDecimal getBalance(String address) {
        try {
            JSONObject param = new JSONObject();
            param.put("account", address);

            JSONArray params = new JSONArray();
            params.add(param);

            JSONObject req = new JSONObject();
            req.put("method", "account_info");
            req.put("params", params);

            JSONObject res = rest.postForObject(url, req, JSONObject.class);
            String balance = res.getJSONObject("result").getJSONObject("account_data").getString("Balance");

            return Util.format(new BigDecimal(balance).divide(BigDecimal.valueOf(Constant.XRP_DIVIDER)), 2);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return BigDecimal.ZERO;
    }

    public String submitTransaction(SubmitTransactionDTO transaction) {
        try {
            JSONObject param = new JSONObject();
            param.put("tx_blob", transaction.getHex());

            JSONArray params = new JSONArray();
            params.add(param);

            JSONObject req = new JSONObject();
            req.put("method", "submit");
            req.put("params", params);

            JSONObject res = rest.postForObject(url, req, JSONObject.class);

            return res.optJSONObject("result").optJSONObject("tx_json").optString("hash");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public CurrentAccountDTO getCurrentAccount(String address) {
        try {
            JSONObject param = new JSONObject();
            param.put("account", address);

            JSONArray params = new JSONArray();
            params.add(param);

            JSONObject req = new JSONObject();
            req.put("method", "account_info");
            req.put("params", params);

            JSONObject res = rest.postForObject(url, req, JSONObject.class);
            Long sequence = res.getJSONObject("result").getJSONObject("account_data").optLong("Sequence");

            return new CurrentAccountDTO(null, sequence);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new CurrentAccountDTO();
    }

    public TransactionStatus getTransactionStatus(String txId) {
        try {
            JSONObject param = new JSONObject();
            param.put("transaction", txId);

            JSONArray params = new JSONArray();
            params.add(param);

            JSONObject req = new JSONObject();
            req.put("method", "tx");
            req.put("params", params);

            JSONObject res = rest.postForObject(url, req, JSONObject.class);

            return TransactionUtil.getRippledTransactionStatus(res.optJSONObject("result").optJSONObject("meta").optString("TransactionResult"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return TransactionStatus.PENDING;
    }

    public TransactionResponseDTO getTransactions(String address, Integer startIndex, Integer limit) {
        try {
            JSONObject param = new JSONObject();
            param.put("account", address);
            param.put("limit", 1000);

            JSONArray params = new JSONArray();
            params.add(param);

            JSONObject req = new JSONObject();
            req.put("method", "account_tx");
            req.put("params", params);

            JSONObject res = rest.postForObject(url, req, JSONObject.class);
            JSONObject jsonResult = res.optJSONObject("result");
            JSONArray array = jsonResult.optJSONArray("transactions");

            if (array != null && !array.isEmpty()) {
                return TransactionUtil.composeRippled(array, address, Constant.XRP_DIVIDER, startIndex, limit);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new TransactionResponseDTO();
    }
}