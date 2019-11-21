package com.batm.service;

import com.batm.dto.CurrentAccountDTO;
import com.batm.dto.SubmitTransactionDTO;
import com.batm.dto.TransactionDTO;
import com.batm.dto.TransactionListDTO;
import com.batm.entity.TransactionRecord;
import com.batm.entity.TransactionRecordGift;
import com.batm.model.TransactionStatus;
import com.batm.model.TransactionType;
import com.batm.util.Constant;
import com.batm.util.Util;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.math.BigDecimal;
import java.util.*;

@Service
public class RippledService {

    @Autowired
    private RestTemplate rest;

    @Value("${xrp.node.url}")
    private String nodeUrl;

    @Value("${xrp.explorer.url}")
    private String explorerUrl;

    public BigDecimal getBalance(String address) {
        try {
            JSONObject param = new JSONObject();
            param.put("account", address);

            JSONArray params = new JSONArray();
            params.add(param);

            JSONObject req = new JSONObject();
            req.put("method", "account_info");
            req.put("params", params);

            JSONObject res = rest.postForObject(nodeUrl, req, JSONObject.class);
            String balance = res.getJSONObject("result").getJSONObject("account_data").getString("Balance");

            return Util.format6(new BigDecimal(balance).divide(Constant.XRP_DIVIDER));
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

            JSONObject res = rest.postForObject(nodeUrl, req, JSONObject.class);

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

            JSONObject res = rest.postForObject(nodeUrl, req, JSONObject.class);
            Long sequence = res.getJSONObject("result").getJSONObject("account_data").optLong("Sequence");

            return new CurrentAccountDTO(null, sequence, null);
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

            JSONObject res = rest.postForObject(nodeUrl, req, JSONObject.class);
            String txResult = res.optJSONObject("result").optJSONObject("meta").optString("TransactionResult");

            return getStatus(txResult);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return TransactionStatus.PENDING;
    }

    public TransactionDTO getTransaction(String txId, String address) {
        TransactionDTO dto = new TransactionDTO();

        try {
            JSONObject param = new JSONObject();
            param.put("transaction", txId);

            JSONArray params = new JSONArray();
            params.add(param);

            JSONObject req = new JSONObject();
            req.put("method", "tx");
            req.put("params", params);

            JSONObject res = rest.postForObject(nodeUrl, req, JSONObject.class);
            JSONObject tx = res.optJSONObject("result");

            dto.setTxId(txId);
            dto.setLink(explorerUrl + "/" + txId);
            dto.setCryptoAmount(getAmount(tx.optString("Amount")));
            dto.setCryptoFee(getAmount(tx.optString("Fee")));
            dto.setFromAddress(tx.optString("Account"));
            dto.setToAddress(tx.optString("Destination"));
            dto.setType(TransactionType.getType(dto.getFromAddress(), dto.getToAddress(), address));
            dto.setStatus(getStatus(tx.optJSONObject("meta").optString("TransactionResult")));
            dto.setDate2(new Date((tx.optLong("date") + 946684800L) * 1000));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return dto;
    }

    public TransactionListDTO getTransactionList(String address, Integer startIndex, Integer limit, List<TransactionRecordGift> gifts, List<TransactionRecord> txs) {
        try {
            JSONObject param = new JSONObject();
            param.put("account", address);
            param.put("limit", 1000);

            JSONArray params = new JSONArray();
            params.add(param);

            JSONObject req = new JSONObject();
            req.put("method", "account_tx");
            req.put("params", params);

            JSONObject res = rest.postForObject(nodeUrl, req, JSONObject.class);
            JSONObject jsonResult = res.optJSONObject("result");
            JSONArray array = jsonResult.optJSONArray("transactions");

            if (array != null && !array.isEmpty()) {
                Map<String, TransactionDTO> map = collectNodeTxs(array, address);

                return Util.buildTxs(map, startIndex, limit, gifts, txs);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new TransactionListDTO();
    }

    private Map<String, TransactionDTO> collectNodeTxs(JSONArray transactionsArray, String address) {
        Map<String, TransactionDTO> map = new HashMap<>();

        for (int i = 0; i < transactionsArray.size(); i++) {
            JSONObject txs = transactionsArray.getJSONObject(i);
            TransactionStatus status = getStatus(txs.optJSONObject("meta").optString("TransactionResult"));

            JSONObject tx = txs.optJSONObject("tx");
            String txId = tx.optString("hash");
            TransactionType type = TransactionType.getType(tx.optString("Account"), tx.optString("Destination"), address);
            BigDecimal amount = Util.format6(getAmount(tx.optString("Amount")));
            Date date1 = new Date((tx.optLong("date") + 946684800L) * 1000);

            map.put(txId, new TransactionDTO(txId, amount, type, status, date1));
        }

        return map;
    }

    private TransactionStatus getStatus(String str) {
        if (StringUtils.isNotEmpty(str) && str.equalsIgnoreCase("tesSUCCESS")) {
            return TransactionStatus.COMPLETE;
        }

        return TransactionStatus.FAIL;
    }

    private BigDecimal getAmount(String amount) {
        return new BigDecimal(amount).divide(Constant.XRP_DIVIDER).stripTrailingZeros();
    }
}