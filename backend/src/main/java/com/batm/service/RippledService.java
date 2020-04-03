package com.batm.service;

import com.batm.dto.*;
import com.batm.model.TransactionStatus;
import com.batm.model.TransactionType;
import com.batm.util.Constant;
import com.batm.util.TxUtil;
import com.batm.util.Util;
import com.google.protobuf.ByteString;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.web3j.utils.Numeric;
import wallet.core.jni.*;
import wallet.core.jni.proto.Ripple;
import java.math.BigDecimal;
import java.util.*;

@Service
public class RippledService {

    @Autowired
    private RestTemplate rest;

    @Autowired
    private WalletService walletService;

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
        } catch (Exception e) {}

        return BigDecimal.ZERO;
    }

    public String submitTransaction(String hex) {
        try {
            JSONObject param = new JSONObject();
            param.put("tx_blob", hex);

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
            Integer sequence = res.getJSONObject("result").getJSONObject("account_data").optInt("Sequence");

            return new CurrentAccountDTO(null, sequence, null);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new CurrentAccountDTO();
    }

    public TransactionDetailsDTO getTransaction(String txId, String address) {
        TransactionDetailsDTO dto = new TransactionDetailsDTO();

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

    public NodeTransactionsDTO getNodeTransactions(String address) {
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

            Map<String, TransactionDetailsDTO> map = collectNodeTxs(array, address);

            return new NodeTransactionsDTO(map);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new NodeTransactionsDTO();
    }

    public TransactionListDTO getTransactionList(String address, Integer startIndex, Integer limit, TxListDTO txDTO) {
        try {
            Map<String, TransactionDetailsDTO> map = getNodeTransactions(address).getMap();

            return TxUtil.buildTxs(map, startIndex, limit, txDTO);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new TransactionListDTO();
    }

    public String sign(String fromAddress, String toAddress, BigDecimal amount, BigDecimal fee) {
        try {
            PrivateKey privateKey;

            if (walletService.isServerAddress(fromAddress)) {
                privateKey = walletService.getPrivateKeyXRP();
            } else {
                String path = walletService.getPath(fromAddress);
                privateKey = walletService.getWallet().getKey(path);
            }

            CurrentAccountDTO accountDTO = getCurrentAccount(fromAddress);

            Ripple.SigningInput.Builder builder = Ripple.SigningInput.newBuilder();
            builder.setAccount(fromAddress);
            builder.setDestination(toAddress);
            builder.setAmount(amount.multiply(Constant.XRP_DIVIDER).longValue());
            builder.setFee(fee.multiply(Constant.XRP_DIVIDER).longValue());
            builder.setSequence(accountDTO.getSequence());
            builder.setPrivateKey(ByteString.copyFrom(privateKey.data()));

            Ripple.SigningOutput sign = RippleSigner.sign(builder.build());
            byte[] bytes = sign.getEncoded().toByteArray();
            String hex = Numeric.toHexString(bytes).substring(2);

            return hex;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private Map<String, TransactionDetailsDTO> collectNodeTxs(JSONArray array, String address) {
        Map<String, TransactionDetailsDTO> map = new HashMap<>();

        if (array != null && !array.isEmpty()) {
            for (int i = 0; i < array.size(); i++) {
                JSONObject txs = array.getJSONObject(i);
                TransactionStatus status = getStatus(txs.optJSONObject("meta").optString("TransactionResult"));

                JSONObject tx = txs.optJSONObject("tx");
                String txId = tx.optString("hash");
                String fromAddress = tx.optString("Account");
                String toAddress = tx.optString("Destination");
                TransactionType type = TransactionType.getType(tx.optString("Account"), tx.optString("Destination"), address);
                BigDecimal amount = Util.format6(getAmount(tx.optString("Amount")));
                Date date1 = new Date((tx.optLong("date") + 946684800L) * 1000);

                map.put(txId, new TransactionDetailsDTO(txId, amount, fromAddress, toAddress, type, status, date1));
            }
        }

        return map;
    }

    private TransactionStatus getStatus(String str) {
        if (StringUtils.isNotBlank(str) && str.equalsIgnoreCase("tesSUCCESS")) {
            return TransactionStatus.COMPLETE;
        }

        return TransactionStatus.FAIL;
    }

    private BigDecimal getAmount(String amount) {
        return new BigDecimal(amount).divide(Constant.XRP_DIVIDER).stripTrailingZeros();
    }
}