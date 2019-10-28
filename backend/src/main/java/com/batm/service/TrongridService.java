package com.batm.service;

import com.batm.dto.CurrentBlockDTO;
import com.batm.dto.SubmitTransactionDTO;
import com.batm.dto.TransactionDTO;
import com.batm.dto.TransactionListDTO;
import com.batm.model.TransactionStatus;
import com.batm.model.TransactionType;
import com.batm.util.Base58;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class TrongridService {

    @Autowired
    private RestTemplate rest;

    @Value("${trx.node.url}")
    private String nodeUrl;

    @Value("${trx.explorer.url}")
    private String explorerUrl;

    public BigDecimal getBalance(String address) {
        try {
            JSONObject res = rest.getForObject(nodeUrl + "/v1/accounts/" + address, JSONObject.class);
            JSONArray data = res.getJSONArray("data");

            if (!data.isEmpty()) {
                String balance = data.getJSONObject(0).getString("balance");

                return Util.format5(new BigDecimal(balance).divide(Constant.TRX_DIVIDER));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return BigDecimal.ZERO;
    }

    public String submitTransaction(SubmitTransactionDTO transaction) {
        try {
            JSONObject res = JSONObject.fromObject(rest.postForObject(nodeUrl + "/wallet/broadcasttransaction", transaction.getTrx(), String.class));

            if (res.optBoolean("result")) {
                return transaction.getTrx().optString("txID");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public TransactionStatus getTransactionStatus(String txId) {
        try {
            JSONObject res = rest.getForObject(nodeUrl + "/v1/transactions/" + txId, JSONObject.class);
            JSONArray array = res.optJSONArray("data");

            if (array != null && !array.isEmpty()) {
                String contractRet = array.getJSONObject(0).optJSONArray("ret").getJSONObject(0).optString("contractRet");

                return getStatus(contractRet);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return TransactionStatus.PENDING;
    }

    public TransactionDTO getTransaction(String txId, String address) {
        TransactionDTO dto = new TransactionDTO();

        try {
            JSONObject res = rest.getForObject(nodeUrl + "/v1/transactions/" + txId, JSONObject.class);
            JSONArray array = res.optJSONArray("data");

            if (array != null && !array.isEmpty()) {
                JSONObject tx = array.getJSONObject(0);
                JSONObject row = tx.optJSONObject("raw_data").optJSONArray("contract").getJSONObject(0).getJSONObject("parameter").optJSONObject("value");

                dto.setTxId(txId);
                dto.setLink(explorerUrl + "/" + txId);
                dto.setCryptoAmount(getAmount(row.optLong("amount")));
                dto.setCryptoFee(getAmount(tx.optJSONObject("raw_data").optLong("fee_limit")));
                dto.setFromAddress(Base58.toBase58(row.optString("owner_address")).toLowerCase());
                dto.setToAddress(Base58.toBase58(row.optString("to_address")).toLowerCase());
                dto.setType(TransactionType.getType(dto.getFromAddress(), dto.getToAddress(), address));
                dto.setStatus(getStatus(tx.optJSONArray("ret").getJSONObject(0).optString("contractRet")));
                dto.setDate2(new Date(tx.optJSONObject("raw_data").optLong("timestamp")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return dto;
    }

    public TransactionListDTO getTransactionList(String address, Integer startIndex, Integer limit) {
        try {
            JSONObject res = rest.getForObject(nodeUrl + "/v1/accounts/" + address + "/transactions?limit=200", JSONObject.class);
            JSONArray array = res.optJSONArray("data");

            if (array != null && !array.isEmpty()) {
                return build(array, address, startIndex, limit);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new TransactionListDTO();
    }

    public CurrentBlockDTO getCurrentBlock() {
        try {
            String resStr = rest.getForObject(nodeUrl + "/wallet/getnowblock", String.class);
            JSONObject res = JSONObject.fromObject(resStr);

            return new CurrentBlockDTO(res.optJSONObject("block_header"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new CurrentBlockDTO();
    }

    private TransactionListDTO build(JSONArray transactionsArray, String address, Integer startIndex, Integer limit) {
        TransactionListDTO result = new TransactionListDTO();

        List<TransactionDTO> transactions = new ArrayList<>();

        int count = 0;
        int k = 0;

        for (int i = 0; i < transactionsArray.size(); i++) {
            JSONObject tx = transactionsArray.getJSONObject(i);

            JSONObject row = tx.optJSONObject("raw_data").optJSONArray("contract").getJSONObject(0).getJSONObject("parameter").optJSONObject("value");

            if (row.containsKey("asset_name")) {
                continue;
            }

            count++;

            if ((i + 1 < startIndex) || ((startIndex + limit) == (i + 1))) {
                continue;
            }

            String txId = tx.optString("txID");
            String fromAddress = Base58.toBase58(row.optString("owner_address")).toLowerCase();
            String toAddress = Base58.toBase58(row.optString("to_address")).toLowerCase();
            String contractRet = tx.optJSONArray("ret").getJSONObject(0).optString("contractRet");
            TransactionType type = TransactionType.getType(fromAddress, toAddress, address);
            BigDecimal amount = Util.format5(getAmount(row.optLong("amount")));
            TransactionStatus status = getStatus(contractRet);
            Date date1 = new Date(tx.optJSONObject("raw_data").optLong("timestamp"));

            transactions.add(new TransactionDTO(startIndex + k, txId, amount, type, status, date1));

            k++;
        }

        result.setTotal(count);
        result.setTransactions(transactions);

        return result;
    }

    private TransactionStatus getStatus(String str) {
        if (StringUtils.isNotEmpty(str) && str.equalsIgnoreCase("SUCCESS")) {
            return TransactionStatus.COMPLETE;
        } else {
            return TransactionStatus.FAIL;
        }
    }

    private BigDecimal getAmount(Long amount) {
        return BigDecimal.valueOf(amount).divide(Constant.TRX_DIVIDER).stripTrailingZeros();
    }
}