package com.batm.service;

import com.batm.dto.*;
import com.batm.entity.TransactionRecord;
import com.batm.entity.TransactionRecordGift;
import com.batm.model.TransactionStatus;
import com.batm.model.TransactionType;
import com.batm.util.Constant;
import com.batm.util.Util;
import com.google.protobuf.ByteString;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.web3j.utils.Numeric;
import wallet.core.jni.*;
import wallet.core.jni.proto.Bitcoin;
import wallet.core.jni.proto.Common;
import wallet.core.jni.proto.Ethereum;
import java.math.BigDecimal;
import java.util.*;

@Service
public class BlockbookService {

    @Autowired
    private RestTemplate rest;

    @Autowired
    private WalletService walletService;

    public BigDecimal getBalance(String url, String address, BigDecimal divider) {
        try {
            JSONObject res = rest.getForObject(url + "/api/v2/address/" + address + "?details=basic", JSONObject.class);

            return Util.format6(new BigDecimal(res.optString("balance")).divide(divider));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return BigDecimal.ZERO;
    }

    public String submitTransaction(String url, String hex) {
        try {
            JSONObject res = rest.getForObject(url + "/api/v2/sendtx/" + hex, JSONObject.class);

            return res.optString("result");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public UtxoDTO getUTXO(String url, String xpub) {
        try {
            JSONArray res = rest.getForObject(url + "/api/v2/utxo/" + xpub, JSONArray.class);

            return new UtxoDTO(res);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new UtxoDTO();
    }

    public NonceDTO getNonce(String url, String address) {
        try {
            JSONObject res = rest.getForObject(url + "/api/v2/address/" + address + "?details=txs&pageSize=1000&page=1", JSONObject.class);

            if (res != null) {
                return new NonceDTO(res.optInt("nonce"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new NonceDTO();
    }

    public TransactionStatus getTransactionStatus(String nodeUrl, String txId) {
        try {
            JSONObject res = rest.getForObject(nodeUrl + "/api/v2/tx/" + txId, JSONObject.class);

            return getStatus(res.optInt("confirmations"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return TransactionStatus.FAIL;
    }

    public TransactionNumberDTO getTransactionNumber(String url, String address, BigDecimal amount, BigDecimal divider, TransactionType type) {
        try {
            JSONObject res = rest.getForObject(url + "/api/v2/address/" + address + "?details=txs&pageSize=1000&page=1", JSONObject.class);
            JSONArray array = res.optJSONArray("transactions");

            if (array != null && !array.isEmpty()) {
                for (int i = 0; i < array.size(); i++) {
                    JSONObject json = array.optJSONObject(i);
                    JSONArray voutArray = json.optJSONArray("vout");

                    for (int j = 0; j < voutArray.size(); j++) {
                        JSONObject voutJson = voutArray.optJSONObject(j);

                        if (voutJson.optJSONArray("addresses").toString().toLowerCase().contains(address.toLowerCase())) {
                            if (type == TransactionType.SELL) {
                                return new TransactionNumberDTO(json.optString("txid"), voutJson.optInt("n"));
                            } else if (type == TransactionType.BUY) {
                                BigDecimal value = new BigDecimal(voutJson.optString("value")).divide(divider);

                                if (amount.compareTo(value) == 0) {
                                    return new TransactionNumberDTO(json.optString("txid"), voutJson.optInt("n"));
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public TransactionDTO getTransaction(String nodeUrl, String explorerUrl, String txId, String address, BigDecimal divider) {
        TransactionDTO dto = new TransactionDTO();

        try {
            JSONObject res = rest.getForObject(nodeUrl + "/api/v2/tx/" + txId, JSONObject.class);
            JSONArray vinArray = res.optJSONArray("vin");
            JSONArray voutArray = res.optJSONArray("vout");

            dto.setTxId(txId);
            dto.setLink(explorerUrl + "/" + txId);
            dto.setType(getType(address, vinArray));
            dto.setCryptoAmount(getAmount(dto.getType(), address, voutArray, divider));
            dto.setCryptoFee(new BigDecimal(res.optString("fees")).divide(divider).stripTrailingZeros());
            dto.setFromAddress(vinArray.getJSONObject(0).optJSONArray("addresses").getString(0));
            dto.setToAddress(getToAddress(voutArray, dto.getFromAddress()));
            dto.setStatus(getStatus(res.optInt("confirmations")));
            dto.setDate2(new Date(res.optLong("blockTime") * 1000));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return dto;
    }

    public BlockchainTransactionsDTO getBlockchainTransactions(String url, String address, BigDecimal divider) {
        try {
            JSONObject res = rest.getForObject(url + "/api/v2/address/" + address + "?details=txs&pageSize=1000&page=1", JSONObject.class);
            JSONArray array = res.optJSONArray("transactions");

            return new BlockchainTransactionsDTO(collectNodeTxs(array, address, divider));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new BlockchainTransactionsDTO();
    }

    public TransactionListDTO getTransactionList(String url, String address, BigDecimal divider, Integer startIndex, Integer limit, List<TransactionRecordGift> gifts, List<TransactionRecord> txs) {
        try {
            Map<String, TransactionDTO> map = getBlockchainTransactions(url, address, divider).getMap();

            return Util.buildTxs(map, startIndex, limit, gifts, txs);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new TransactionListDTO();
    }

    public String signETH(String ethNodeUrl, String toAddress, BigDecimal amount, PrivateKey privateKey) {
        try {
            String fromAddress = new EthereumAddress(privateKey.getPublicKeySecp256k1(false)).description();
            Integer nonce = getNonce(ethNodeUrl, fromAddress).getNonce();

            Ethereum.SigningInput.Builder builder = Ethereum.SigningInput.newBuilder();

            builder.setPrivateKey(ByteString.copyFrom(Numeric.hexStringToByteArray(Numeric.toHexStringNoPrefix(privateKey.data()))));
            builder.setToAddress(toAddress);
            builder.setChainId(ByteString.copyFrom(Numeric.hexStringToByteArray("1")));
            builder.setNonce(ByteString.copyFrom(Numeric.hexStringToByteArray(Integer.toHexString(nonce))));
            builder.setGasPrice(ByteString.copyFrom(Numeric.hexStringToByteArray(Long.toHexString(Constant.GAS_PRICE))));
            builder.setGasLimit(ByteString.copyFrom(Numeric.hexStringToByteArray(Long.toHexString(Constant.GAS_LIMIT))));
            builder.setAmount(ByteString.copyFrom(Numeric.hexStringToByteArray(Long.toHexString(amount.multiply(Constant.ETH_DIVIDER).longValue()))));

            Ethereum.SigningOutput output = EthereumSigner.sign(builder.build());

            return Numeric.toHexString(output.getEncoded().toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public String signBTCForks(CoinType coinType, String fromAddress, String toAddress, BigDecimal amount, BigDecimal fee, BigDecimal divider, List<JSONObject> utxos) {
        try {
            Bitcoin.SigningInput.Builder signerBuilder = Bitcoin.SigningInput.newBuilder();
            signerBuilder.setCoinType(coinType.value());
            signerBuilder.setAmount(amount.multiply(divider).longValue());
            signerBuilder.setByteFee(fee.multiply(divider).longValue());
            signerBuilder.setHashType(coinType == CoinType.BITCOINCASH ? 65 : 1);
            signerBuilder.setChangeAddress(fromAddress);
            signerBuilder.setToAddress(toAddress);

            utxos.forEach(e -> {
                PrivateKey privateKey = walletService.getWallet().getKey(e.optString("path"));
                signerBuilder.addPrivateKey(ByteString.copyFrom(privateKey.data()));
            });

            utxos.forEach(e -> {
                BitcoinScript redeemScript = BitcoinScript.buildForAddress(e.optString("address"), coinType);
                byte[] keyHash = redeemScript.isPayToWitnessScriptHash() ? redeemScript.matchPayToWitnessPublicKeyHash() : redeemScript.matchPayToPubkeyHash();

                if (keyHash.length > 0) {
                    String key = Numeric.toHexString(keyHash);
                    ByteString scriptByteString = ByteString.copyFrom(redeemScript.data());
                    signerBuilder.putScripts(key, scriptByteString);
                }
            });

            for (int index = 0; index < utxos.size(); index++) {
                JSONObject utxo = utxos.get(index);

                byte[] hash = Numeric.hexStringToByteArray(utxo.optString("txid"));
                Collections.reverse(Arrays.asList(hash));

                Bitcoin.OutPoint.Builder outPointBuilder = Bitcoin.OutPoint.newBuilder();
                outPointBuilder.setHash(ByteString.copyFrom(hash));
                outPointBuilder.setIndex(utxo.optInt("vout"));
                outPointBuilder.setSequence(Integer.MAX_VALUE - utxos.size() + index);
                Bitcoin.OutPoint outPoint = outPointBuilder.build();

                BitcoinScript redeemScript = BitcoinScript.buildForAddress(utxo.optString("address"), coinType);
                ByteString scriptByteString = ByteString.copyFrom(redeemScript.data());

                Bitcoin.UnspentTransaction.Builder unspent = Bitcoin.UnspentTransaction.newBuilder();
                unspent.setScript(scriptByteString);
                unspent.setAmount(Long.parseLong(utxo.optString("value")));
                unspent.setOutPoint(outPoint);

                Bitcoin.UnspentTransaction unspentBuild = unspent.build();
                signerBuilder.addUtxo(unspentBuild);
            }

            BitcoinTransactionSigner signer = new BitcoinTransactionSigner(signerBuilder.build());
            Common.Result result = signer.sign();
            Bitcoin.SigningOutput output = result.getObjects(0).unpack(Bitcoin.SigningOutput.class);

            return Numeric.toHexString(output.getEncoded().toByteArray()).substring(2);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private Map<String, TransactionDTO> collectNodeTxs(JSONArray array, String address, BigDecimal divider) {
        Map<String, TransactionDTO> map = new HashMap<>();

        if (array != null && !array.isEmpty()) {
            for (int i = 0; i < array.size(); i++) {
                JSONObject json = array.getJSONObject(i);
                JSONArray vinArray = json.optJSONArray("vin");
                JSONArray voutArray = json.optJSONArray("vout");

                String txId = json.optString("txid");
                TransactionType type = getType(address, vinArray);
                BigDecimal amount = Util.format6(getAmount(type, address, voutArray, divider));
                TransactionStatus status = getStatus(json.optInt("confirmations"));
                Date date1 = new Date(json.optLong("blockTime") * 1000);

                map.put(txId, new TransactionDTO(txId, amount, type, status, date1));
            }
        }

        return map;
    }

    private TransactionStatus getStatus(Integer confirmations) {
        return (confirmations == null || confirmations < 2) ? TransactionStatus.PENDING : TransactionStatus.COMPLETE;
    }

    private BigDecimal getAmount(TransactionType type, String address, JSONArray array, BigDecimal divider) {
        for (int i = 0; i < array.size(); i++) {
            JSONObject json = array.getJSONObject(i);

            if ((type == TransactionType.WITHDRAW && !json.optJSONArray("addresses").toString().toLowerCase().contains(address.toLowerCase())) ||
                    (type == TransactionType.DEPOSIT && json.optJSONArray("addresses").toString().toLowerCase().contains(address.toLowerCase()))) {

                return new BigDecimal(json.optString("value")).divide(divider).stripTrailingZeros();
            }
        }

        return null;
    }

    private TransactionType getType(String address, JSONArray array) {
        for (int i = 0; i < array.size(); i++) {
            JSONObject json = array.getJSONObject(i);

            return json.optJSONArray("addresses").toString().toLowerCase().contains(address.toLowerCase()) ? TransactionType.WITHDRAW : TransactionType.DEPOSIT;
        }

        return null;
    }

    private String getToAddress(JSONArray array, String address) {
        for (int i = 0; i < array.size(); i++) {
            if (!array.getJSONObject(i).optJSONArray("addresses").toString().toLowerCase().contains(address)) {
                return array.getJSONObject(i).optJSONArray("addresses").getString(0);
            }
        }

        return null;
    }
}