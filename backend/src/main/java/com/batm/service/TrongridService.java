package com.batm.service;

import com.batm.dto.CurrentBlockDTO;
import com.batm.dto.TransactionDTO;
import com.batm.dto.TransactionListDTO;
import com.batm.entity.TransactionRecord;
import com.batm.entity.TransactionRecordGift;
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
import java.util.*;

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

                return Util.format6(new BigDecimal(balance).divide(Constant.TRX_DIVIDER));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return BigDecimal.ZERO;
    }

    public String submitTransaction(JSONObject json) {
        try {
            JSONObject res = JSONObject.fromObject(rest.postForObject(nodeUrl + "/wallet/broadcasttransaction", json, String.class));

            if (res.optBoolean("result")) {
                return json.optString("txID");
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

        return TransactionStatus.FAIL;
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

    public TransactionListDTO getTransactionList(String address, Integer startIndex, Integer limit, List<TransactionRecordGift> gifts, List<TransactionRecord> txs) {
        try {
            JSONObject res = rest.getForObject(nodeUrl + "/v1/accounts/" + address + "/transactions?limit=200", JSONObject.class);
            JSONArray array = res.optJSONArray("data");
            Map<String, TransactionDTO> map = collectNodeTxs(array, address);

            return Util.buildTxs(map, startIndex, limit, gifts, txs);
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

    private Map<String, TransactionDTO> collectNodeTxs(JSONArray array, String address) {
        Map<String, TransactionDTO> map = new HashMap<>();

        if (array != null && !array.isEmpty()) {
            for (int i = 0; i < array.size(); i++) {
                JSONObject tx = array.getJSONObject(i);

                JSONObject row = tx.optJSONObject("raw_data").optJSONArray("contract").getJSONObject(0).getJSONObject("parameter").optJSONObject("value");

                if (row.containsKey("asset_name")) {
                    continue;
                }

                String txId = tx.optString("txID");
                String fromAddress = Base58.toBase58(row.optString("owner_address")).toLowerCase();
                String toAddress = Base58.toBase58(row.optString("to_address")).toLowerCase();
                String contractRet = tx.optJSONArray("ret").getJSONObject(0).optString("contractRet");
                TransactionType type = TransactionType.getType(fromAddress, toAddress, address);
                BigDecimal amount = Util.format6(getAmount(row.optLong("amount")));
                TransactionStatus status = getStatus(contractRet);
                Date date1 = new Date(tx.optJSONObject("raw_data").optLong("timestamp"));

                map.put(txId, new TransactionDTO(txId, amount, type, status, date1));
            }
        }

        return map;
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