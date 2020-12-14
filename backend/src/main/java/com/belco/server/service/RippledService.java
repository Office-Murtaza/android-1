package com.belco.server.service;

import com.belco.server.dto.*;
import com.belco.server.model.TransactionStatus;
import com.belco.server.model.TransactionType;
import com.belco.server.util.TxUtil;
import com.belco.server.util.Util;
import com.google.protobuf.ByteString;
import lombok.Getter;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.web3j.utils.Numeric;
import wallet.core.java.AnySigner;
import wallet.core.jni.CoinType;
import wallet.core.jni.PrivateKey;
import wallet.core.jni.proto.Ripple;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Getter
@Service
public class RippledService {

    private static final BigDecimal XRP_DIVIDER = BigDecimal.valueOf(1_000_000L);
    private static final CoinType XRP = CoinType.XRP;

    private final RestTemplate rest;
    private final WalletService walletService;
    private final MonitorService monitorService;

    @Value("${xrp.explorer.url}")
    private String explorerUrl;

    public RippledService(RestTemplate rest, WalletService walletService, MonitorService monitorService) {
        this.rest = rest;
        this.walletService = walletService;
        this.monitorService = monitorService;
    }

    public BigDecimal getBalance(String address) {
        if (monitorService.isNodeAvailable(XRP)) {
            try {
                JSONObject param = new JSONObject();
                param.put("account", address);

                JSONArray params = new JSONArray();
                params.add(param);

                JSONObject req = new JSONObject();
                req.put("method", "account_info");
                req.put("params", params);

                JSONObject res = rest.postForObject(monitorService.getNodeUrl(XRP), req, JSONObject.class);
                JSONObject result = res.optJSONObject("result");

                if (result != null) {
                    JSONObject accountData = result.optJSONObject("account_data");

                    if (accountData != null) {
                        String balance = accountData.optString("Balance");

                        return Util.format(new BigDecimal(balance).divide(XRP_DIVIDER), 6);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();

                if (monitorService.switchToReserveNode(XRP)) {
                    return getBalance(address);
                }
            }
        }

        return BigDecimal.ZERO;
    }

    public String submitTransaction(String hex) {
        if (monitorService.isNodeAvailable(XRP)) {
            try {
                JSONObject param = new JSONObject();
                param.put("tx_blob", hex);

                JSONArray params = new JSONArray();
                params.add(param);

                JSONObject req = new JSONObject();
                req.put("method", "submit");
                req.put("params", params);

                JSONObject res = rest.postForObject(monitorService.getNodeUrl(XRP), req, JSONObject.class);
                String txId = res.optJSONObject("result").optJSONObject("tx_json").optString("hash");

                if (isTransactionExist(txId)) {
                    return txId;
                }
            } catch (Exception e) {
                e.printStackTrace();

                if (monitorService.switchToReserveNode(XRP)) {
                    return submitTransaction(hex);
                }
            }
        }

        return null;
    }

    public CurrentAccountDTO getCurrentAccount(String address) {
        if (monitorService.isNodeAvailable(XRP)) {
            try {
                JSONObject param = new JSONObject();
                param.put("account", address);

                JSONArray params = new JSONArray();
                params.add(param);

                JSONObject req = new JSONObject();
                req.put("method", "account_info");
                req.put("params", params);

                JSONObject res = rest.postForObject(monitorService.getNodeUrl(XRP), req, JSONObject.class);
                Integer sequence = res.getJSONObject("result").getJSONObject("account_data").optInt("Sequence");

                return new CurrentAccountDTO(null, sequence, null);
            } catch (Exception e) {
                e.printStackTrace();

                if (monitorService.switchToReserveNode(XRP)) {
                    return getCurrentAccount(address);
                }
            }
        }

        return new CurrentAccountDTO();
    }

    public TransactionDetailsDTO getTransaction(String txId, String address) {
        TransactionDetailsDTO dto = new TransactionDetailsDTO();

        if (monitorService.isNodeAvailable(XRP)) {
            try {
                JSONObject param = new JSONObject();
                param.put("transaction", txId);

                JSONArray params = new JSONArray();
                params.add(param);

                JSONObject req = new JSONObject();
                req.put("method", "tx");
                req.put("params", params);

                JSONObject res = rest.postForObject(monitorService.getNodeUrl(XRP), req, JSONObject.class);
                JSONObject tx = res.optJSONObject("result");

                dto.setTxId(txId);
                dto.setLink(explorerUrl + "/" + txId);
                dto.setCryptoAmount(getAmount(tx.optString("Amount")));
                dto.setCryptoFee(getAmount(tx.optString("Fee")));
                dto.setFromAddress(tx.optString("Account"));
                dto.setToAddress(tx.optString("Destination"));
                dto.setType(TransactionType.getType(dto.getFromAddress(), dto.getToAddress(), address));

                JSONObject meta = tx.optJSONObject("meta");

                if (meta != null) {
                    dto.setStatus(getStatus(meta.optString("TransactionResult")));
                } else {
                    dto.setStatus(TransactionStatus.FAIL);
                }

                dto.setDate2(new Date((tx.optLong("date") + 946684800L) * 1000));
            } catch (Exception e) {
                e.printStackTrace();

                if (monitorService.switchToReserveNode(XRP)) {
                    return getTransaction(txId, address);
                }
            }
        }

        return dto;
    }

    public NodeTransactionsDTO getNodeTransactions(String address) {
        if (monitorService.isNodeAvailable(XRP)) {
            try {
                JSONObject param = new JSONObject();
                param.put("account", address);
                param.put("limit", 1000);

                JSONArray params = new JSONArray();
                params.add(param);

                JSONObject req = new JSONObject();
                req.put("method", "account_tx");
                req.put("params", params);

                JSONObject res = rest.postForObject(monitorService.getNodeUrl(XRP), req, JSONObject.class);
                JSONObject jsonResult = res.optJSONObject("result");
                JSONArray array = jsonResult.optJSONArray("transactions");

                Map<String, TransactionDetailsDTO> map = collectNodeTxs(array, address);

                return new NodeTransactionsDTO(map);
            } catch (Exception e) {
                e.printStackTrace();

                if (monitorService.switchToReserveNode(XRP)) {
                    return getNodeTransactions(address);
                }
            }
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

            if (walletService.isServerAddress(CoinType.XRP, fromAddress)) {
                privateKey = walletService.getCoinsMap().get(CoinType.XRP).getPrivateKey();
            } else {
                String path = walletService.getPath(fromAddress);
                privateKey = walletService.getWallet().getKey(CoinType.XRP, path);
            }

            CurrentAccountDTO accountDTO = getCurrentAccount(fromAddress);

            Ripple.SigningInput.Builder builder = Ripple.SigningInput.newBuilder();
            builder.setAccount(fromAddress);
            builder.setDestination(toAddress);
            builder.setAmount(amount.multiply(XRP_DIVIDER).longValue());
            builder.setFee(fee.multiply(XRP_DIVIDER).longValue());
            builder.setSequence(accountDTO.getSequence());
            builder.setPrivateKey(ByteString.copyFrom(privateKey.data()));

            Ripple.SigningOutput sign = AnySigner.sign(builder.build(), CoinType.XRP, Ripple.SigningOutput.parser());
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
                BigDecimal amount = Util.format(getAmount(tx.optString("Amount")), 6);
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
        if (StringUtils.isNotBlank(amount)) {
            return new BigDecimal(amount).divide(XRP_DIVIDER).stripTrailingZeros();
        }

        return BigDecimal.ZERO;
    }

    private boolean isTransactionExist(String txId) {
        if (monitorService.isNodeAvailable(XRP)) {
            try {
                JSONObject param = new JSONObject();
                param.put("transaction", txId);

                JSONArray params = new JSONArray();
                params.add(param);

                JSONObject req = new JSONObject();
                req.put("method", "tx");
                req.put("params", params);

                JSONObject res = rest.postForObject(monitorService.getNodeUrl(XRP), req, JSONObject.class);
                JSONObject tx = res.optJSONObject("result");

                return !tx.optString("status").equalsIgnoreCase("error");
            } catch (Exception e) {
                e.printStackTrace();

                if (monitorService.switchToReserveNode(XRP)) {
                    return isTransactionExist(txId);
                }
            }
        }

        return false;
    }
}