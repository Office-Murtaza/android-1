package com.belco.server.service;

import com.belco.server.dto.TransactionDetailsDTO;
import com.belco.server.dto.TransactionHistoryDTO;
import com.belco.server.entity.TransactionRecord;
import com.belco.server.model.TransactionStatus;
import com.belco.server.model.TransactionType;
import com.belco.server.util.Util;
import com.google.protobuf.ByteString;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.web3j.utils.Numeric;
import wallet.core.java.AnySigner;
import wallet.core.jni.BitcoinScript;
import wallet.core.jni.CoinType;
import wallet.core.jni.PrivateKey;
import wallet.core.jni.proto.Bitcoin;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class BlockbookService {

    public static final BigDecimal DIVIDER = BigDecimal.valueOf(100_000_000L);

    private final RestTemplate rest;
    private final WalletService walletService;
    private final NodeService nodeService;

    public BlockbookService(RestTemplate rest, WalletService walletService, NodeService nodeService) {
        this.rest = rest;
        this.walletService = walletService;
        this.nodeService = nodeService;
    }

    public BigDecimal getBalance(CoinType coinType, String address) {
        if (nodeService.isNodeAvailable(coinType)) {
            try {
                JSONObject res = rest.getForObject(nodeService.getNodeUrl(coinType) + "/api/v2/address/" + address + "?details=basic", JSONObject.class);

                return Util.format(new BigDecimal(res.optString("balance")).divide(DIVIDER), 6);
            } catch (Exception e) {
                e.printStackTrace();

                if (nodeService.switchToReserveNode(coinType)) {
                    return getBalance(coinType, address);
                }
            }
        }

        return BigDecimal.ZERO;
    }

    public String submitTransaction(CoinType coinType, String hex) {
        if (nodeService.isNodeAvailable(coinType)) {
            try {
                JSONObject res = rest.getForObject(nodeService.getNodeUrl(coinType) + "/api/v2/sendtx/" + hex, JSONObject.class);

                return res.optString("result");
            } catch (Exception e) {
                e.printStackTrace();

                if (nodeService.switchToReserveNode(coinType)) {
                    return submitTransaction(coinType, hex);
                }
            }
        }

        return null;
    }

    public JSONArray getUtxo(CoinType coinType, String xpub) {
        if (nodeService.isNodeAvailable(coinType)) {
            try {
                return rest.getForObject(nodeService.getNodeUrl(coinType) + "/api/v2/utxo/" + xpub, JSONArray.class);
            } catch (Exception e) {
                e.printStackTrace();

                if (nodeService.switchToReserveNode(coinType)) {
                    return getUtxo(coinType, xpub);
                }
            }
        }

        return new JSONArray();
    }

    public Long getByteFee(CoinType coinType) {
        if (nodeService.isNodeAvailable(coinType)) {
            try {
                JSONObject res = rest.getForObject(nodeService.getNodeUrl(coinType) + "/api/v2/estimatefee/3", JSONObject.class);

                return new BigDecimal(res.optString("result")).divide(new BigDecimal(1000)).multiply(DIVIDER).longValue();
            } catch (Exception e) {
                e.printStackTrace();

                if (nodeService.switchToReserveNode(coinType)) {
                    return getByteFee(coinType);
                }
            }
        }

        return null;
    }

    public boolean isTransactionSeenOnBlockchain(CoinType coinType, String txId) {
        if (nodeService.isNodeAvailable(coinType)) {
            try {
                return rest.getForObject(nodeService.getNodeUrl(coinType) + "/api/v2/tx/" + txId, JSONObject.class) != null;
            } catch (HttpClientErrorException he) {
                return false;
            } catch (Exception e) {
                e.printStackTrace();

                if (nodeService.switchToReserveNode(coinType)) {
                    return isTransactionSeenOnBlockchain(coinType, txId);
                }
            }
        }

        return false;
    }

    public TransactionDetailsDTO getTransactionDetails(CoinType coinType, String txId, String address) {
        TransactionDetailsDTO dto = new TransactionDetailsDTO();

        if (nodeService.isNodeAvailable(coinType)) {
            try {
                JSONObject res = rest.getForObject(nodeService.getNodeUrl(coinType) + "/api/v2/tx/" + txId, JSONObject.class);
                dto = extractTransactionDetails(coinType, address, res);
            } catch (HttpClientErrorException he) {
                dto.setStatus(TransactionStatus.NOT_EXIST.getValue());
            } catch (Exception e) {
                e.printStackTrace();

                if (nodeService.switchToReserveNode(coinType)) {
                    return getTransactionDetails(coinType, txId, address);
                }
            }
        }

        return dto;
    }

    private TransactionDetailsDTO extractTransactionDetails(CoinType coinType, String address, JSONObject res) {
        JSONArray vin = res.optJSONArray("vin");
        JSONArray vout = res.optJSONArray("vout");

        String txId = res.optString("txid");
        String fromAddress = getFromAddress(vin, address);
        String toAddress = getToAddress(vout, address, fromAddress);
        TransactionType type = TransactionType.getType(fromAddress, toAddress, address);
        BigDecimal amount = Util.format(getAmount(type, fromAddress, toAddress, vout, DIVIDER), 6);

        TransactionDetailsDTO tx = new TransactionDetailsDTO();
        tx.setTxId(txId);
        tx.setLink(nodeService.getExplorerUrl(coinType) + "/" + txId);

        if(type != null) {
            tx.setType(type.getValue());
        }

        tx.setCryptoAmount(amount);
        tx.setFromAddress(fromAddress);
        tx.setToAddress(toAddress);
        tx.setCryptoFee(Util.format(new BigDecimal(res.optString("fees")).divide(DIVIDER), 6));
        tx.setConfirmations(res.optInt("confirmations"));
        tx.setStatus(getStatus(res.optInt("confirmations")).getValue());
        tx.setTimestamp(res.optLong("blockTime") * 1000);

        return tx;
    }

    public Map<String, TransactionDetailsDTO> getNodeTransactions(CoinType coinType, String address) {
        if (nodeService.isNodeAvailable(coinType)) {
            try {
                JSONObject res = rest.getForObject(nodeService.getNodeUrl(coinType) + "/api/v2/address/" + address + "?details=txs&pageSize=1000&page=1", JSONObject.class);
                JSONArray array = res.optJSONArray("transactions");

                return collectNodeTxs(coinType, array, address);
            } catch (Exception e) {
                e.printStackTrace();

                if (nodeService.switchToReserveNode(coinType)) {
                    return getNodeTransactions(coinType, address);
                }
            }
        }

        return Collections.emptyMap();
    }

    public TransactionHistoryDTO getTransactionHistory(CoinType coinType, String address, List<TransactionRecord> transactionRecords, List<TransactionDetailsDTO> details) {
        return TransactionService.buildTxs(getNodeTransactions(coinType, address), transactionRecords, details);
    }

    public String signBTCForks(Long walletId, CoinType coinType, String fromAddress, String toAddress, BigDecimal amount, Long byteFee, List<JSONObject> utxos) {
        try {
            Bitcoin.SigningInput.Builder input = Bitcoin.SigningInput.newBuilder();
            input.setCoinType(coinType.value());
            input.setAmount(amount.multiply(DIVIDER).longValue());
            input.setByteFee(byteFee);
            input.setHashType(BitcoinScript.hashTypeForCoin(coinType));
            input.setChangeAddress(fromAddress);
            input.setToAddress(toAddress);
            input.setUseMaxAmount(false);

            utxos.forEach(e -> {
                PrivateKey privateKey = walletService.get(walletId).getWallet().getKey(coinType, e.optString("path"));
                input.addPrivateKey(ByteString.copyFrom(privateKey.data()));
            });

            utxos.forEach(e -> {
                BitcoinScript redeemScript = BitcoinScript.lockScriptForAddress(e.optString("address"), coinType);
                byte[] keyHash = redeemScript.isPayToWitnessPublicKeyHash() ? redeemScript.matchPayToWitnessPublicKeyHash() : redeemScript.matchPayToPubkeyHash();

                if (keyHash.length > 0) {
                    String key = Numeric.toHexString(keyHash);
                    ByteString scriptByteString = ByteString.copyFrom(redeemScript.data());
                    input.putScripts(key, scriptByteString);
                }
            });

            for (int index = 0; index < utxos.size(); index++) {
                JSONObject utxo = utxos.get(index);

                byte[] hash = Numeric.hexStringToByteArray(utxo.optString("txid"));
                Util.reverse(hash);

                Bitcoin.OutPoint.Builder output = Bitcoin.OutPoint.newBuilder();
                output.setHash(ByteString.copyFrom(hash));
                output.setIndex(utxo.optInt("vout"));
                output.setSequence(Integer.MAX_VALUE - utxos.size() + index);
                Bitcoin.OutPoint outPoint = output.build();

                BitcoinScript redeemScript = BitcoinScript.lockScriptForAddress(utxo.optString("address"), coinType);
                ByteString scriptByteString = ByteString.copyFrom(redeemScript.data());

                Bitcoin.UnspentTransaction.Builder unspent = Bitcoin.UnspentTransaction.newBuilder();
                unspent.setScript(scriptByteString);
                unspent.setAmount(Long.parseLong(utxo.optString("value")));
                unspent.setOutPoint(outPoint);

                Bitcoin.UnspentTransaction unspentBuild = unspent.build();
                input.addUtxo(unspentBuild);
            }

            Bitcoin.SigningOutput signer = AnySigner.sign(input.build(), coinType, Bitcoin.SigningOutput.parser());

            return Numeric.toHexString(signer.getEncoded().toByteArray()).substring(2);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public BigDecimal getTxFee(CoinType coinType) {
        return BigDecimal.valueOf(getByteFee(coinType)).divide(DIVIDER).multiply(BigDecimal.valueOf(1000)).stripTrailingZeros();
    }

    private Map<String, TransactionDetailsDTO> collectNodeTxs(CoinType coinType, JSONArray array, String address) {
        Map<String, TransactionDetailsDTO> map = new HashMap<>();

        if (array != null && !array.isEmpty()) {
            for (int i = 0; i < array.size(); i++) {
                JSONObject res = array.getJSONObject(i);
                TransactionDetailsDTO tx = extractTransactionDetails(coinType, address, res);
                map.put(tx.getTxId(), tx);
            }
        }

        return map;
    }

    private TransactionStatus getStatus(Integer confirmations) {
        return (confirmations == null || confirmations < 2) ? TransactionStatus.PENDING : TransactionStatus.COMPLETE;
    }

    private BigDecimal getAmount(TransactionType type, String fromAddress, String toAddress, JSONArray voutArray, BigDecimal divider) {
        BigDecimal amount = BigDecimal.ZERO;

        for (int i = 0; i < voutArray.size(); i++) {
            JSONObject json = voutArray.getJSONObject(i);

            if (type == TransactionType.SELF) {
                if (json.optJSONArray("addresses").toString().toLowerCase().contains(toAddress.toLowerCase())) {
                    amount = new BigDecimal(json.optString("value"));
                    break;
                }
            } else if (type == TransactionType.WITHDRAW) {
                if (!json.optJSONArray("addresses").toString().toLowerCase().contains(fromAddress.toLowerCase())) {
                    amount = new BigDecimal(json.optString("value"));
                    break;
                }
            } else if (type == TransactionType.DEPOSIT) {
                if (json.optJSONArray("addresses").toString().toLowerCase().contains(toAddress.toLowerCase())) {
                    amount = amount.add(new BigDecimal(json.optString("value")));
                }
            }
        }

        return amount.divide(divider).stripTrailingZeros();
    }

    private String getFromAddress(JSONArray vinArray, String address) {
        for (int i = 0; i < vinArray.size(); i++) {
            JSONObject json = vinArray.optJSONObject(i);

            if (json.optJSONArray("addresses").toString().toLowerCase().contains(address.toLowerCase())) {
                return address;
            }
        }

        return vinArray.optJSONObject(0).optJSONArray("addresses").optString(0);
    }

    private String getToAddress(JSONArray voutArray, String address, String fromAddress) {
        if (!fromAddress.equalsIgnoreCase(address)) {
            return address;
        } else {
            for (int i = 0; i < voutArray.size(); i++) {
                if (!voutArray.getJSONObject(i).optJSONArray("addresses").toString().toLowerCase().contains(fromAddress)) {
                    return voutArray.getJSONObject(i).optJSONArray("addresses").optString(0);
                }
            }

            return voutArray.optJSONObject(0).optJSONArray("addresses").optString(0);
        }
    }
}