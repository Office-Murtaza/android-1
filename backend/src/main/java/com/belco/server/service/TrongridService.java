package com.belco.server.service;

import com.belco.server.dto.*;
import com.belco.server.model.TransactionStatus;
import com.belco.server.model.TransactionType;
import com.belco.server.util.Base58;
import com.belco.server.util.TxUtil;
import com.belco.server.util.Util;
import com.google.protobuf.ByteString;
import lombok.Getter;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.web3j.utils.Numeric;
import wallet.core.java.AnySigner;
import wallet.core.jni.CoinType;
import wallet.core.jni.PrivateKey;
import wallet.core.jni.proto.Tron;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.*;

@Getter
@Service
public class TrongridService {

    private static final BigDecimal TRX_DIVIDER = BigDecimal.valueOf(1_000_000L);

    @Autowired
    private RestTemplate rest;

    @Autowired
    private WalletService walletService;

    @Value("${trx.node.url}")
    private String nodeUrl;

    @Value("${trx.explorer.url}")
    private String explorerUrl;

    private boolean isNodeAvailable;

    @PostConstruct
    public void init() {
        if (org.apache.commons.lang3.StringUtils.isNotBlank(nodeUrl)) {
            isNodeAvailable = true;
        }
    }

    public BigDecimal getBalance(String address) {
        if (isNodeAvailable) {
            try {
                JSONObject json = new JSONObject();
                json.put("address", Base58.toHex(address));

                JSONObject res = JSONObject.fromObject(rest.postForObject(nodeUrl + "/wallet/getaccount", json, String.class));
                String balance = res.optString("balance");

                if (StringUtils.isNotBlank(balance)) {
                    return Util.format(new BigDecimal(balance).divide(TRX_DIVIDER), 6);
                }
            } catch (ResourceAccessException rae) {
                isNodeAvailable = false;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return BigDecimal.ZERO;
    }

    public String submitTransaction(String hex) {
        if (isNodeAvailable) {
            try {
                JSONObject json = JSONObject.fromObject(hex);
                JSONObject res = JSONObject.fromObject(rest.postForObject(nodeUrl + "/wallet/broadcasttransaction", json, String.class));

                if (res.optBoolean("result")) {
                    return json.optString("txID");
                }
            } catch (ResourceAccessException rae) {
                isNodeAvailable = false;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    public TransactionDetailsDTO getTransaction(String txId, String address) {
        TransactionDetailsDTO dto = new TransactionDetailsDTO();

        if (isNodeAvailable) {
            try {
                JSONObject json = new JSONObject();
                json.put("value", txId);

                JSONObject res = JSONObject.fromObject(rest.postForObject(nodeUrl + "/wallet/gettransactionbyid", json, String.class));
                JSONObject row = res.optJSONObject("raw_data").optJSONArray("contract").getJSONObject(0).getJSONObject("parameter").optJSONObject("value");

                dto.setTxId(txId);
                dto.setLink(explorerUrl + "/" + txId);
                dto.setCryptoAmount(getAmount(row.optLong("amount")));
                dto.setCryptoFee(getAmount(res.optJSONObject("raw_data").optLong("fee_limit")));
                dto.setFromAddress(Base58.toBase58(row.optString("owner_address")));
                dto.setToAddress(Base58.toBase58(row.optString("to_address")));
                dto.setType(TransactionType.getType(dto.getFromAddress(), dto.getToAddress(), address));
                dto.setStatus(getStatus(res.optJSONArray("ret").getJSONObject(0).optString("contractRet")));
                dto.setConfirmations(dto.getStatus().getConfirmations());
                dto.setDate2(new Date(res.optJSONObject("raw_data").optLong("timestamp")));
            } catch (ResourceAccessException rae) {
                isNodeAvailable = false;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return dto;
    }

    public NodeTransactionsDTO getNodeTransactions(String address) {
        if (isNodeAvailable) {
            try {
                JSONObject res = rest.getForObject(nodeUrl + "/v1/accounts/" + address + "/transactions?limit=200&search_internal=true", JSONObject.class);
                JSONArray array = res.optJSONArray("data");
                Map<String, TransactionDetailsDTO> map = collectNodeTxs(array, address);

                return new NodeTransactionsDTO(map);
            } catch (ResourceAccessException rae) {
                isNodeAvailable = false;
            } catch (Exception e) {
                e.printStackTrace();
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

    public CurrentBlockDTO getCurrentBlock() {
        if (isNodeAvailable) {
            try {
                String resStr = rest.getForObject(nodeUrl + "/wallet/getnowblock", String.class);
                JSONObject res = JSONObject.fromObject(resStr);

                return new CurrentBlockDTO(res.optJSONObject("block_header"));
            } catch (ResourceAccessException rae) {
                isNodeAvailable = false;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return new CurrentBlockDTO();
    }

    public String sign(String fromAddress, String toAddress, BigDecimal amount, BigDecimal fee) {
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
            transactionBuilder.setFeeLimit(fee.multiply(TRX_DIVIDER).longValue());
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
                BigDecimal amount = Util.format(getAmount(row.optLong("amount")), 6);
                TransactionStatus status = getStatus(contractRet);
                Date date1 = new Date(tx.optJSONObject("raw_data").optLong("timestamp"));

                map.put(txId, new TransactionDetailsDTO(txId, amount, fromAddress, toAddress, type, status, date1));
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