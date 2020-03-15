package com.batm.service;

import com.batm.dto.NodeTransactionsDTO;
import com.batm.dto.CurrentBlockDTO;
import com.batm.dto.TransactionDTO;
import com.batm.dto.TransactionListDTO;
import com.batm.entity.TransactionRecord;
import com.batm.entity.TransactionRecordGift;
import com.batm.model.TransactionStatus;
import com.batm.model.TransactionType;
import com.batm.util.Base58;
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
import wallet.core.jni.PrivateKey;
import wallet.core.jni.TronSigner;
import wallet.core.jni.proto.Tron;
import java.math.BigDecimal;
import java.util.*;

@Service
public class TrongridService {

    @Autowired
    private RestTemplate rest;

    @Autowired
    private WalletService walletService;

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
        } catch (Exception e) {}

        return BigDecimal.ZERO;
    }

    public String submitTransaction(String hex) {
        try {
            JSONObject json = JSONObject.fromObject(hex);
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
                dto.setFromAddress(Base58.toBase58(row.optString("owner_address")));
                dto.setToAddress(Base58.toBase58(row.optString("to_address")));
                dto.setType(TransactionType.getType(dto.getFromAddress(), dto.getToAddress(), address));
                dto.setStatus(getStatus(tx.optJSONArray("ret").getJSONObject(0).optString("contractRet")));
                dto.setConfirmations(dto.getStatus().getConfirmations());
                dto.setDate2(new Date(tx.optJSONObject("raw_data").optLong("timestamp")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return dto;
    }

    public NodeTransactionsDTO getNodeTransactions(String address) {
        try {
            JSONObject res = rest.getForObject(nodeUrl + "/v1/accounts/" + address + "/transactions?limit=200", JSONObject.class);
            JSONArray array = res.optJSONArray("data");
            Map<String, TransactionDTO> map = collectNodeTxs(array, address);

            return new NodeTransactionsDTO(map);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new NodeTransactionsDTO();
    }

    public TransactionListDTO getTransactionList(String address, Integer startIndex, Integer limit, List<TransactionRecordGift> gifts, List<TransactionRecord> txs) {
        try {
            Map<String, TransactionDTO> map = getNodeTransactions(address).getMap();

            return TxUtil.buildTxs(map, startIndex, limit, gifts, txs);
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

    public String sign(String fromAddress, String toAddress, BigDecimal amount, BigDecimal fee) {
        try {
            PrivateKey privateKey;

            if (walletService.isServerAddress(fromAddress)) {
                privateKey = walletService.getPrivateKeyTRX();
            } else {
                String path = walletService.getPath(fromAddress);
                privateKey = walletService.getWallet().getKey(path);
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
            transferBuilder.setAmount(amount.multiply(Constant.TRX_DIVIDER).longValue());

            Tron.Transaction.Builder transactionBuilder = Tron.Transaction.newBuilder();
            transactionBuilder.setTransfer(transferBuilder.build());
            transactionBuilder.setTimestamp(System.currentTimeMillis());
            transactionBuilder.setExpiration(transactionBuilder.getTimestamp() + 36000000);
            transactionBuilder.setFeeLimit(fee.multiply(Constant.TRX_DIVIDER).longValue());
            transactionBuilder.setBlockHeader(headerBuilder.build());

            Tron.SigningInput.Builder sign = Tron.SigningInput.newBuilder();
            sign.setTransaction(transactionBuilder.build());
            sign.setPrivateKey(ByteString.copyFrom(Numeric.hexStringToByteArray(Numeric.toHexStringNoPrefix(privateKey.data()))));

            Tron.SigningOutput output = TronSigner.sign(sign.build());

            return output.getJson();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
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
                String fromAddress = Base58.toBase58(row.optString("owner_address"));
                String toAddress = Base58.toBase58(row.optString("to_address"));
                String contractRet = tx.optJSONArray("ret").getJSONObject(0).optString("contractRet");
                TransactionType type = TransactionType.getType(fromAddress, toAddress, address);
                BigDecimal amount = Util.format6(getAmount(row.optLong("amount")));
                TransactionStatus status = getStatus(contractRet);
                Date date1 = new Date(tx.optJSONObject("raw_data").optLong("timestamp"));

                map.put(txId, new TransactionDTO(txId, amount, fromAddress, toAddress, type, status, date1));
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
        return BigDecimal.valueOf(amount).divide(Constant.TRX_DIVIDER).stripTrailingZeros();
    }
}