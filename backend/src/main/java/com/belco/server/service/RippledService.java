package com.belco.server.service;

import com.belco.server.dto.CurrentAccountDTO;
import com.belco.server.dto.TransactionDetailsDTO;
import com.belco.server.dto.TransactionHistoryDTO;
import com.belco.server.entity.TransactionRecord;
import com.belco.server.entity.TransactionRecordWallet;
import com.belco.server.model.TransactionStatus;
import com.belco.server.model.TransactionType;
import com.belco.server.util.TxUtil;
import com.belco.server.util.Util;
import com.google.protobuf.ByteString;
import lombok.Getter;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.web3j.utils.Numeric;
import wallet.core.java.AnySigner;
import wallet.core.jni.CoinType;
import wallet.core.jni.PrivateKey;
import wallet.core.jni.proto.Ripple;
import java.math.BigDecimal;
import java.util.*;

@Getter
@Service
public class RippledService {

    private static final BigDecimal XRP_DIVIDER = BigDecimal.valueOf(1_000_000L);
    private static final CoinType COIN_TYPE = CoinType.XRP;

    private final RestTemplate rest;
    private final WalletService walletService;
    private final NodeService nodeService;

    public RippledService(RestTemplate rest, WalletService walletService, NodeService nodeService) {
        this.rest = rest;
        this.walletService = walletService;
        this.nodeService = nodeService;
    }

    public BigDecimal getBalance(String address) {
        if (nodeService.isNodeAvailable(COIN_TYPE)) {
            try {
                JSONObject param = new JSONObject();
                param.put("account", address);

                JSONArray params = new JSONArray();
                params.add(param);

                JSONObject req = new JSONObject();
                req.put("method", "account_info");
                req.put("params", params);

                JSONObject res = rest.postForObject(nodeService.getNodeUrl(COIN_TYPE), req, JSONObject.class);
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

                if (nodeService.switchToReserveNode(COIN_TYPE)) {
                    return getBalance(address);
                }
            }
        }

        return BigDecimal.ZERO;
    }

    public BigDecimal getTxFee() {
        if (nodeService.isNodeAvailable(COIN_TYPE)) {
            try {
                JSONObject req = new JSONObject();
                req.put("method", "fee");
                req.put("params", new JSONArray());

                JSONObject res = rest.postForObject(nodeService.getNodeUrl(COIN_TYPE), req, JSONObject.class);
                JSONObject result = res.optJSONObject("result");

                if (result != null) {
                    JSONObject drops = result.optJSONObject("drops");

                    if (drops != null) {
                        String balance = drops.optString("base_fee");

                        return Util.format(new BigDecimal(balance).divide(XRP_DIVIDER), 6);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();

                if (nodeService.switchToReserveNode(COIN_TYPE)) {
                    return getTxFee();
                }
            }
        }

        return BigDecimal.ZERO;
    }

    public String submitTransaction(String hex) {
        if (nodeService.isNodeAvailable(COIN_TYPE)) {
            try {
                JSONObject param = new JSONObject();
                param.put("tx_blob", hex);

                JSONArray params = new JSONArray();
                params.add(param);

                JSONObject req = new JSONObject();
                req.put("method", "submit");
                req.put("params", params);

                JSONObject res = rest.postForObject(nodeService.getNodeUrl(COIN_TYPE), req, JSONObject.class);
                String txId = res.optJSONObject("result").optJSONObject("tx_json").optString("hash");

                if (isTransactionExist(txId)) {
                    return txId;
                }
            } catch (Exception e) {
                e.printStackTrace();

                if (nodeService.switchToReserveNode(COIN_TYPE)) {
                    return submitTransaction(hex);
                }
            }
        }

        return null;
    }

    public CurrentAccountDTO getCurrentAccount(String address) {
        if (nodeService.isNodeAvailable(COIN_TYPE)) {
            try {
                JSONObject param = new JSONObject();
                param.put("account", address);

                JSONArray params = new JSONArray();
                params.add(param);

                JSONObject req = new JSONObject();
                req.put("method", "account_info");
                req.put("params", params);

                JSONObject res = rest.postForObject(nodeService.getNodeUrl(COIN_TYPE), req, JSONObject.class);
                Integer sequence = res.getJSONObject("result").getJSONObject("account_data").optInt("Sequence");

                return new CurrentAccountDTO(null, sequence, null);
            } catch (Exception e) {
                e.printStackTrace();

                if (nodeService.switchToReserveNode(COIN_TYPE)) {
                    return getCurrentAccount(address);
                }
            }
        }

        return new CurrentAccountDTO();
    }

    public boolean isTransactionSeenOnBlockchain(String txId) {
        if (nodeService.isNodeAvailable(COIN_TYPE)) {
            try {
                JSONArray params = new JSONArray();
                params.add(Util.toJsonObject("transaction", txId));

                JSONObject req = new JSONObject();
                req.put("method", "tx");
                req.put("params", params);

                JSONObject res = rest.postForObject(nodeService.getNodeUrl(COIN_TYPE), req, JSONObject.class).optJSONObject("result");

                if(res.containsKey("error")) {
                    if(res.optJSONObject("result").optString("error").equalsIgnoreCase("txnNotFound")) {
                        return false;
                    }
                } else {
                    return true;
                }
            } catch (Exception e) {
                e.printStackTrace();

                if (nodeService.switchToReserveNode(COIN_TYPE)) {
                    return isTransactionSeenOnBlockchain(txId);
                }
            }
        }

        return false;
    }

    public TransactionDetailsDTO getTransactionDetails(String txId, String address, String explorerUrl) {
        TransactionDetailsDTO dto = new TransactionDetailsDTO();

        if (nodeService.isNodeAvailable(COIN_TYPE)) {
            try {
                JSONArray params = new JSONArray();
                params.add(Util.toJsonObject("transaction", txId));

                JSONObject req = new JSONObject();
                req.put("method", "tx");
                req.put("params", params);

                JSONObject res = rest.postForObject(nodeService.getNodeUrl(COIN_TYPE), req, JSONObject.class).optJSONObject("result");

                dto.setTxId(txId);
                dto.setLink(explorerUrl + "/" + txId);
                dto.setCryptoAmount(getAmount(res.optString("Amount")));
                dto.setCryptoFee(getAmount(res.optString("Fee")));
                dto.setFromAddress(res.optString("Account"));
                dto.setToAddress(res.optString("Destination"));
                dto.setType(TransactionType.getType(dto.getFromAddress(), dto.getToAddress(), address));
                dto.setStatus(getStatus(res.optString("status")));
                dto.setDate2(new Date((res.optLong("date") + 946684800L) * 1000));
            } catch (Exception e) {
                e.printStackTrace();

                if (nodeService.switchToReserveNode(COIN_TYPE)) {
                    return getTransactionDetails(txId, address, explorerUrl);
                }
            }
        }

        return dto;
    }

    public Map<String, TransactionDetailsDTO> getNodeTransactions(String address) {
        if (nodeService.isNodeAvailable(COIN_TYPE)) {
            try {
                JSONObject param = new JSONObject();
                param.put("account", address);
                param.put("limit", 1000);

                JSONArray params = new JSONArray();
                params.add(param);

                JSONObject req = new JSONObject();
                req.put("method", "account_tx");
                req.put("params", params);

                JSONObject res = rest.postForObject(nodeService.getNodeUrl(COIN_TYPE), req, JSONObject.class).optJSONObject("result");
                JSONArray array = res.optJSONArray("transactions");

                return collectNodeTxs(array, address);
            } catch (Exception e) {
                e.printStackTrace();

                if (nodeService.switchToReserveNode(COIN_TYPE)) {
                    return getNodeTransactions(address);
                }
            }
        }

        return Collections.emptyMap();
    }

    public TransactionHistoryDTO getTransactionDetails(String address, Integer startIndex, Integer limit, List<TransactionRecord> transactionRecords, List<TransactionRecordWallet> transactionRecordWallets) {
        return TxUtil.buildTxs(getNodeTransactions(address), startIndex, limit, transactionRecords, transactionRecordWallets);
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
        if (StringUtils.isNotBlank(str) && str.equalsIgnoreCase("success")) {
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
        if (nodeService.isNodeAvailable(COIN_TYPE)) {
            try {
                JSONObject param = new JSONObject();
                param.put("transaction", txId);

                JSONArray params = new JSONArray();
                params.add(param);

                JSONObject req = new JSONObject();
                req.put("method", "tx");
                req.put("params", params);

                JSONObject res = rest.postForObject(nodeService.getNodeUrl(COIN_TYPE), req, JSONObject.class).optJSONObject("result");

                return !res.optString("status").equalsIgnoreCase("error");
            } catch (Exception e) {
                e.printStackTrace();

                if (nodeService.switchToReserveNode(COIN_TYPE)) {
                    return isTransactionExist(txId);
                }
            }
        }

        return false;
    }
}