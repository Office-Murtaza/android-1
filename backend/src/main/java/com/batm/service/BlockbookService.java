package com.batm.service;

import com.batm.dto.*;
import com.batm.entity.TransactionRecord;
import com.batm.entity.TransactionRecordGift;
import com.batm.model.TransactionStatus;
import com.batm.model.TransactionType;
import com.batm.util.Util;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class BlockbookService {

    @Autowired
    private RestTemplate rest;

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

        return TransactionStatus.PENDING;
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
                                BigDecimal value = new BigDecimal(voutJson.optString("value")).divide(divider).stripTrailingZeros();

                                if (amount.equals(value)) {
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

    public TransactionListDTO getTransactionList(String url, String address, BigDecimal divider, Integer startIndex, Integer limit, List<TransactionRecordGift> gifts, List<TransactionRecord> txs) {
        try {
            JSONObject res = rest.getForObject(url + "/api/v2/address/" + address + "?details=txs&pageSize=1000&page=1", JSONObject.class);
            JSONArray array = res.optJSONArray("transactions");

            if (array != null && !array.isEmpty()) {
                Map<String, TransactionDTO> map = collectNodeTxs(array, address, divider);

                return Util.buildTxs(map, startIndex, limit, gifts, txs);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new TransactionListDTO();
    }

    private Map<String, TransactionDTO> collectNodeTxs(JSONArray array, String address, BigDecimal divider) {
        Map<String, TransactionDTO> map = new HashMap<>();

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