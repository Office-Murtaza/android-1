package com.belco.server.service;

import com.belco.server.dto.*;
import com.belco.server.model.TransactionStatus;
import com.belco.server.model.TransactionType;
import com.belco.server.util.TxUtil;
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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class BlockbookService {

    public static final BigDecimal BTC_DIVIDER = BigDecimal.valueOf(100_000_000L);

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

                return Util.format(new BigDecimal(res.optString("balance")).divide(BTC_DIVIDER), 6);
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

    public UtxoDTO getUTXO(CoinType coinType, String xpub) {
        if (nodeService.isNodeAvailable(coinType)) {
            try {
                JSONArray res = rest.getForObject(nodeService.getNodeUrl(coinType) + "/api/v2/utxo/" + xpub, JSONArray.class);

                return new UtxoDTO(res);
            } catch (Exception e) {
                e.printStackTrace();

                if (nodeService.switchToReserveNode(coinType)) {
                    return getUTXO(coinType, xpub);
                }
            }
        }

        return new UtxoDTO();
    }

    public Long getByteFee(CoinType coinType) {
        if (nodeService.isNodeAvailable(coinType)) {
            try {
                JSONObject res = rest.getForObject(nodeService.getNodeUrl(coinType) + "/api/v2/estimatefee/2", JSONObject.class);

                return new BigDecimal(res.optString("result")).divide(new BigDecimal(1000)).multiply(BTC_DIVIDER).longValue();
            } catch (Exception e) {
                e.printStackTrace();

                if (nodeService.switchToReserveNode(coinType)) {
                    return getByteFee(coinType);
                }
            }
        }

        return null;
    }

    public TransactionNumberDTO getTransactionNumber(CoinType coinType, String address, BigDecimal amount, TransactionType type) {
        if (nodeService.isNodeAvailable(coinType)) {
            try {
                JSONObject res = rest.getForObject(nodeService.getNodeUrl(coinType) + "/api/v2/address/" + address + "?details=txs&pageSize=1000&page=1", JSONObject.class);
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
                                    BigDecimal value = new BigDecimal(voutJson.optString("value")).divide(BTC_DIVIDER);

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

                if (nodeService.switchToReserveNode(coinType)) {
                    return getTransactionNumber(coinType, address, amount, type);
                }
            }
        }

        return null;
    }

    public TransactionDetailsDTO getTransaction(CoinType coinType, String txId, String address, String explorerUrl) {
        TransactionDetailsDTO dto = new TransactionDetailsDTO();

        if (nodeService.isNodeAvailable(coinType)) {
            try {
                JSONObject res = rest.getForObject(nodeService.getNodeUrl(coinType) + "/api/v2/tx/" + txId, JSONObject.class);
                JSONArray vinArray = res.optJSONArray("vin");
                JSONArray voutArray = res.optJSONArray("vout");

                String fromAddress = getFromAddress(vinArray, address);
                String toAddress = getToAddress(voutArray, address, fromAddress);
                TransactionType type = TransactionType.getType(fromAddress, toAddress, address);
                BigDecimal amount = Util.format(getAmount(type, fromAddress, toAddress, voutArray, BTC_DIVIDER), 6);

                dto.setTxId(txId);
                dto.setLink(explorerUrl + "/" + txId);
                dto.setType(type);
                dto.setCryptoAmount(amount);
                dto.setFromAddress(fromAddress);
                dto.setToAddress(toAddress);
                dto.setCryptoFee(new BigDecimal(res.optString("fees")).divide(BTC_DIVIDER).stripTrailingZeros());
                dto.setStatus(getStatus(res.optInt("confirmations")));
                dto.setDate2(new Date(res.optLong("blockTime") * 1000));
            } catch (HttpClientErrorException he) {
                dto.setStatus(TransactionStatus.FAIL);
            } catch (Exception e) {
                e.printStackTrace();

                if (nodeService.switchToReserveNode(coinType)) {
                    return getTransaction(coinType, txId, address, explorerUrl);
                }
            }
        }

        return dto;
    }

    public NodeTransactionsDTO getNodeTransactions(CoinType coinType, String address) {
        if (nodeService.isNodeAvailable(coinType)) {
            try {
                JSONObject res = rest.getForObject(nodeService.getNodeUrl(coinType) + "/api/v2/address/" + address + "?details=txs&pageSize=1000&page=1", JSONObject.class);
                JSONArray array = res.optJSONArray("transactions");

                return new NodeTransactionsDTO(collectNodeTxs(array, address));
            } catch (Exception e) {
                e.printStackTrace();

                if (nodeService.switchToReserveNode(coinType)) {
                    return getNodeTransactions(coinType, address);
                }
            }
        }

        return new NodeTransactionsDTO();
    }

    public TransactionListDTO getTransactionList(CoinType coinType, String address, Integer startIndex, Integer limit, TxListDTO txDTO) {
        try {
            Map<String, TransactionDetailsDTO> map = getNodeTransactions(coinType, address).getMap();

            return TxUtil.buildTxs(map, startIndex, limit, txDTO);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new TransactionListDTO();
    }

    public String signBTCForks(CoinType coinType, String fromAddress, String toAddress, BigDecimal amount, Long byteFee, List<JSONObject> utxos) {
        try {
            Bitcoin.SigningInput.Builder input = Bitcoin.SigningInput.newBuilder();
            input.setCoinType(coinType.value());
            input.setAmount(amount.multiply(BTC_DIVIDER).longValue());
            input.setByteFee(byteFee);
            input.setHashType(BitcoinScript.hashTypeForCoin(coinType));
            input.setChangeAddress(fromAddress);
            input.setToAddress(toAddress);
            input.setUseMaxAmount(false);

            //utxos = utxos.stream().filter(e -> Long.parseLong(e.optString("value")) > byteFee * 180).collect(Collectors.toList());

            utxos.forEach(e -> {
                PrivateKey privateKey = walletService.getWallet().getKey(coinType, e.optString("path"));
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
        return BigDecimal.valueOf(getByteFee(coinType)).divide(BTC_DIVIDER).multiply(BigDecimal.valueOf(1000)).stripTrailingZeros();
    }

    private Map<String, TransactionDetailsDTO> collectNodeTxs(JSONArray array, String address) {
        Map<String, TransactionDetailsDTO> map = new HashMap<>();

        if (array != null && !array.isEmpty()) {
            for (int i = 0; i < array.size(); i++) {
                JSONObject json = array.getJSONObject(i);
                JSONArray vinArray = json.optJSONArray("vin");
                JSONArray voutArray = json.optJSONArray("vout");

                String txId = json.optString("txid");
                String fromAddress = getFromAddress(vinArray, address);
                String toAddress = getToAddress(voutArray, address, fromAddress);
                TransactionType type = TransactionType.getType(fromAddress, toAddress, address);
                BigDecimal amount = Util.format(getAmount(type, fromAddress, toAddress, voutArray, BTC_DIVIDER), 6);

                TransactionStatus status = getStatus(json.optInt("confirmations"));
                Date date1 = new Date(json.optLong("blockTime") * 1000);

                map.put(txId, new TransactionDetailsDTO(txId, amount, fromAddress, toAddress, type, status, date1));
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