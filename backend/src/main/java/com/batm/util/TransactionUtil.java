package com.batm.util;

import com.batm.dto.TransactionListDTO;
import com.batm.dto.TransactionDTO;
import com.batm.model.TransactionStatus;
import com.batm.model.TransactionType;
import com.binance.dex.api.client.domain.TransactionPage;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TransactionUtil {

    public static TransactionListDTO composeBlockbook(Integer total, JSONArray transactionsArray, String address, BigDecimal divider, Integer startIndex, Integer limit) {
        TransactionListDTO result = new TransactionListDTO();
        List<TransactionDTO> transactions = new ArrayList<>();

        for (int i = 0; i < transactionsArray.size(); i++) {
            if (i + 1 < startIndex) {
                continue;
            }

            transactions.add(parse(startIndex + i, address, divider, transactionsArray.getJSONObject(i)));

            if ((startIndex + limit) == (i + 1)) {
                break;
            }
        }

        result.setTotal(total);
        result.setTransactions(transactions);

        return result;
    }

    private static TransactionDTO parse(Integer index, String address, BigDecimal divider, JSONObject json) {
        List<JSONObject> vinList = Util.jsonArrayToList(json.optJSONArray("vin"));
        List<JSONObject> voutList = Util.jsonArrayToList(json.optJSONArray("vout"));

        String txId = json.getString("txid");
        TransactionType type = getTransactionType(address, vinList);
        BigDecimal value = getTransactionValue(type, address, voutList, divider);
        TransactionStatus status = getTransactionStatus(json.optInt("confirmations"));
        Date date = new Date(json.optLong("blockTime") * 1000);

        return new TransactionDTO(index, txId, value, type, status, date);
    }

    private static BigDecimal getTransactionValue(String value, BigDecimal divider) {
        if (value != null && value.length() > 0) {
            return new BigDecimal(value).divide(divider).stripTrailingZeros();
        }

        return null;
    }

    private static TransactionStatus getTransactionStatus(Integer confirmations) {
        return (confirmations == null || confirmations < 3) ? TransactionStatus.PENDING : TransactionStatus.COMPLETE;
    }

    private static TransactionType getTransactionType(String address, List<JSONObject> vinList) {
        return vinList.stream().anyMatch(e -> e.getJSONArray("addresses").toString().toLowerCase().contains(address.toLowerCase())) ? TransactionType.WITHDRAW : TransactionType.DEPOSIT;
    }

    private static BigDecimal getTransactionValue(TransactionType type, String address, List<JSONObject> voutList, BigDecimal divider) {
        if (type == TransactionType.WITHDRAW) {
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