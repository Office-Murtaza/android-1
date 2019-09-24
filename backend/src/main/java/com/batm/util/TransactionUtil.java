package com.batm.util;

import com.batm.dto.TransactionDTO;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TransactionUtil {

    public static List<TransactionDTO> compose(JSONArray transactionsArray, String address, Long divider, Integer fromIndex, Integer limit) {
        List<TransactionDTO> transactions = new ArrayList<>();

        for (int i = 0; i < transactionsArray.size(); i++) {
            if (i + 1 < fromIndex) {
                continue;
            }

            transactions.add(parse(fromIndex + i, address, divider, transactionsArray.getJSONObject(i)));

            if ((fromIndex + limit) == (i + 1)) {
                break;
            }
        }

        return transactions;
    }

    private static TransactionDTO parse(Integer index, String address, Long divider, JSONObject json) {
        List<JSONObject> vinList = Util.jsonArrayToList(json.optJSONArray("vin"));
        List<JSONObject> voutList = Util.jsonArrayToList(json.optJSONArray("vout"));

        String txId = json.getString("txid");
        TransactionDTO.TransactionType type = getTransactionType(address, vinList);
        BigDecimal value = getTransactionValue(type, address, voutList, divider);
        TransactionDTO.TransactionStatus status = getTransactionStatus(json.optInt("confirmations"));
        Date date = new Date(json.optLong("blockTime") * 1000);

        return new TransactionDTO(index, txId, value, status, type, date);
    }

    private static BigDecimal getTransactionValue(String value, Long divider) {
        if (value != null && value.length() > 0) {
            return new BigDecimal(value).divide(BigDecimal.valueOf(divider)).stripTrailingZeros();
        }

        return null;
    }

    private static TransactionDTO.TransactionStatus getTransactionStatus(Integer confirmations) {
        return (confirmations == null || confirmations < 3) ? TransactionDTO.TransactionStatus.PENDING : TransactionDTO.TransactionStatus.COMPLETE;
    }

    private static TransactionDTO.TransactionType getTransactionType(String address, List<JSONObject> vinList) {
        return vinList.stream().anyMatch(e -> e.getJSONArray("addresses").toString().toLowerCase().contains(address.toLowerCase())) ? TransactionDTO.TransactionType.WITHDRAW : TransactionDTO.TransactionType.DEPOSIT;
    }

    private static BigDecimal getTransactionValue(TransactionDTO.TransactionType type, String address, List<JSONObject> voutList, Long divider) {
        if (type == TransactionDTO.TransactionType.WITHDRAW) {
            return getTransactionValue(voutList.stream().filter(vout -> !vout.getJSONArray("addresses").toString().toLowerCase().contains(address.toLowerCase()))
                    .findFirst()
                    .get().getString("value"), divider);
        } else {
            return getTransactionValue(voutList.stream().filter(vout -> vout.getJSONArray("addresses").toString().toLowerCase().contains(address.toLowerCase()))
                    .findFirst()
                    .get().getString("value"), divider);
        }
    }
}