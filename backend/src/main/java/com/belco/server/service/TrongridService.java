package com.belco.server.service;

import com.belco.server.dto.CurrentBlockDTO;
import com.belco.server.dto.TransactionDetailsDTO;
import com.belco.server.dto.TransactionHistoryDTO;
import com.belco.server.entity.TransactionRecord;
import com.belco.server.model.TransactionStatus;
import com.belco.server.model.TransactionType;
import com.belco.server.util.Base58;
import com.belco.server.util.Util;
import com.google.protobuf.ByteString;
import lombok.Getter;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.web3j.utils.Numeric;
import wallet.core.java.AnySigner;
import wallet.core.jni.CoinType;
import wallet.core.jni.PrivateKey;
import wallet.core.jni.proto.Tron;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Service
public class TrongridService {

    private static final BigDecimal TRX_DIVIDER = BigDecimal.valueOf(1_000_000L);
    private static final CoinType COIN_TYPE = CoinType.TRON;

    private final RestTemplate rest;
    private final WalletService walletService;
    private final NodeService nodeService;

    public TrongridService(RestTemplate rest, WalletService walletService, NodeService nodeService) {
        this.rest = rest;
        this.walletService = walletService;
        this.nodeService = nodeService;
    }

    public BigDecimal getBalance(String address) {
        if (nodeService.isNodeAvailable(COIN_TYPE)) {
            try {
                JSONObject json = new JSONObject();
                json.put("address", Base58.toHex(address));

                JSONObject res = JSONObject.fromObject(rest.postForObject(nodeService.getNodeUrl(COIN_TYPE) + "/wallet/getaccount", json, String.class));
                String balance = res.optString("balance");

                if (StringUtils.isNotBlank(balance)) {
                    return Util.format(new BigDecimal(balance).divide(TRX_DIVIDER), 6);
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

    public String submitTransaction(String hex) {
        if (nodeService.isNodeAvailable(COIN_TYPE)) {
            try {
                JSONObject json = JSONObject.fromObject(hex);
                JSONObject res = JSONObject.fromObject(rest.postForObject(nodeService.getNodeUrl(COIN_TYPE) + "/wallet/broadcasttransaction", json, String.class));

                if (res.optBoolean("result")) {
                    return json.optString("txID");
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

    public boolean isTransactionSeenOnBlockchain(String txId) {
        if (nodeService.isNodeAvailable(COIN_TYPE)) {
            try {
                JSONObject json = new JSONObject();
                json.put("value", txId);

                JSONObject res = JSONObject.fromObject(rest.postForObject(nodeService.getNodeUrl(COIN_TYPE) + "/wallet/gettransactionbyid", json, String.class));

                return !res.isEmpty();
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
                JSONObject json = new JSONObject();
                json.put("value", txId);

                JSONObject res = JSONObject.fromObject(rest.postForObject(nodeService.getNodeUrl(COIN_TYPE) + "/wallet/gettransactionbyid", json, String.class));
                JSONObject row = res.optJSONObject("raw_data").optJSONArray("contract").getJSONObject(0).getJSONObject("parameter").optJSONObject("value");

                dto.setTxId(txId);
                dto.setLink(explorerUrl + "/" + txId);
                dto.setCryptoAmount(getAmount(row.optLong("amount")));
                dto.setCryptoFee(getAmount(res.optJSONObject("raw_data").optLong("fee_limit")));
                dto.setFromAddress(Base58.toBase58(row.optString("owner_address")));
                dto.setToAddress(Base58.toBase58(row.optString("to_address")));
                dto.setType(TransactionType.getType(dto.getFromAddress(), dto.getToAddress(), address).getValue());
                dto.setStatus(getStatus(res.optJSONArray("ret").getJSONObject(0).optString("contractRet")).getValue());
                dto.setConfirmations(TransactionStatus.valueOf(dto.getStatus()).getConfirmations());
                dto.setTimestamp(res.optJSONObject("raw_data").optLong("timestamp"));
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
                JSONObject res = rest.getForObject(nodeService.getNodeUrl(COIN_TYPE) + "/v1/accounts/" + address + "/transactions?limit=200&search_internal=true", JSONObject.class);
                JSONArray array = res.optJSONArray("data");

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

    public TransactionHistoryDTO getTransactionHistory(String address, Integer startIndex, Integer limit, List<TransactionRecord> transactionRecords, List<TransactionDetailsDTO> details) {
        return TransactionService.buildTxs(getNodeTransactions(address), startIndex, limit, transactionRecords, details);
    }

    public CurrentBlockDTO getCurrentBlock() {
        if (nodeService.isNodeAvailable(COIN_TYPE)) {
            try {
                String resStr = rest.getForObject(nodeService.getNodeUrl(COIN_TYPE) + "/wallet/getnowblock", String.class);
                JSONObject res = JSONObject.fromObject(resStr);

                return new CurrentBlockDTO(res.optJSONObject("block_header"));
            } catch (Exception e) {
                e.printStackTrace();

                if (nodeService.switchToReserveNode(COIN_TYPE)) {
                    return getCurrentBlock();
                }
            }
        }

        return new CurrentBlockDTO();
    }

    public String sign(String fromAddress, String toAddress, BigDecimal amount) {
        try {
            PrivateKey privateKey;

            if (walletService.isServerAddress(CoinType.TRON, fromAddress)) {
                privateKey = walletService.getCoinsMap().get(CoinType.TRON).getPrivateKey();
            } else {
                String path = walletService.getPath(fromAddress);
                privateKey = walletService.getWallet().getKey(CoinType.TRON, path);
            }

            CurrentBlockDTO currentBlockDTO = getCurrentBlock();
            JSONObject rawData = currentBlockDTO.getBlockHeader().optJSONObject("raw_data");

            Tron.BlockHeader.Builder headerBuilder = Tron.BlockHeader.newBuilder();
            headerBuilder.setNumber(rawData.optLong("number"));
            headerBuilder.setTimestamp(rawData.optLong("timestamp"));
            headerBuilder.setVersion(rawData.optInt("version"));
            headerBuilder.setParentHash(ByteString.copyFrom(Numeric.hexStringToByteArray(rawData.optString("parentHash"))));
            headerBuilder.setWitnessAddress(ByteString.copyFrom(Numeric.hexStringToByteArray(rawData.optString("witness_address"))));
            headerBuilder.setTxTrieRoot(ByteString.copyFrom(Numeric.hexStringToByteArray(rawData.optString("txTrieRoot"))));

            Tron.TransferContract.Builder transferBuilder = Tron.TransferContract.newBuilder();
            transferBuilder.setOwnerAddress(fromAddress);
            transferBuilder.setToAddress(toAddress);
            transferBuilder.setAmount(amount.multiply(TRX_DIVIDER).longValue());

            Tron.Transaction.Builder transactionBuilder = Tron.Transaction.newBuilder();
            transactionBuilder.setTransfer(transferBuilder.build());
            transactionBuilder.setTimestamp(System.currentTimeMillis());
            transactionBuilder.setExpiration(transactionBuilder.getTimestamp() + 36000000);
            transactionBuilder.setBlockHeader(headerBuilder.build());

            Tron.SigningInput.Builder sign = Tron.SigningInput.newBuilder();
            sign.setTransaction(transactionBuilder.build());
            sign.setPrivateKey(ByteString.copyFrom(Numeric.hexStringToByteArray(Numeric.toHexStringNoPrefix(privateKey.data()))));

            Tron.SigningOutput output = AnySigner.sign(sign.build(), CoinType.TRON, Tron.SigningOutput.parser());

            return output.getJson();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private Map<String, TransactionDetailsDTO> collectNodeTxs(JSONArray array, String address) {
        Map<String, TransactionDetailsDTO> map = new HashMap<>();

        if (array != null && !array.isEmpty()) {
            for (int i = 0; i < array.size(); i++) {
                JSONObject t = array.getJSONObject(i);
                JSONObject row = t.optJSONObject("raw_data");

                if (row.containsKey("contract")) {
                    row = row.optJSONArray("contract").getJSONObject(0).getJSONObject("parameter").optJSONObject("value");
                } else {
                    row = row.optJSONArray("token").getJSONObject(0).getJSONObject("parameter").optJSONObject("value");
                }

                if (row.containsKey("asset_name")) {
                    continue;
                }

                String txId = t.optString("txID");
                String fromAddress = Base58.toBase58(row.optString("owner_address"));
                String toAddress = Base58.toBase58(row.optString("to_address"));
                String contractRet = t.optJSONArray("ret").getJSONObject(0).optString("contractRet");
                TransactionType type = TransactionType.getType(fromAddress, toAddress, address);
                BigDecimal amount = Util.format(getAmount(row.optLong("amount")), 6);
                TransactionStatus status = getStatus(contractRet);
                long timestamp = t.optJSONObject("raw_data").optLong("timestamp");

                TransactionDetailsDTO tx = new TransactionDetailsDTO();
                tx.setTxId(txId);
                tx.setType(type.getValue());
                tx.setStatus(status.getValue());
                tx.setCryptoAmount(amount);
                tx.setFromAddress(fromAddress);
                tx.setToAddress(toAddress);
                tx.setTimestamp(timestamp);

                map.put(txId, tx);
            }
        }

        return map;
    }

    private TransactionStatus getStatus(String str) {
        if (StringUtils.isNotBlank(str) && str.equalsIgnoreCase("SUCCESS")) {
            return TransactionStatus.COMPLETE;
        } else {
            return TransactionStatus.FAIL;
        }
    }

    private BigDecimal getAmount(Long amount) {
        return BigDecimal.valueOf(amount).divide(TRX_DIVIDER).stripTrailingZeros();
    }
}