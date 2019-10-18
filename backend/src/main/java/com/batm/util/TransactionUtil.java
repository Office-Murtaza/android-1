package com.batm.util;

import com.batm.dto.TransactionResponseDTO;
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

    public static TransactionResponseDTO composeBlockbook(Integer total, JSONArray transactionsArray, String address, Long divider, Integer startIndex, Integer limit) {
        TransactionResponseDTO result = new TransactionResponseDTO();
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

    private static TransactionDTO parse(Integer index, String address, Long divider, JSONObject json) {
        List<JSONObject> vinList = Util.jsonArrayToList(json.optJSONArray("vin"));
        List<JSONObject> voutList = Util.jsonArrayToList(json.optJSONArray("vout"));

        String txId = json.getString("txid");
        TransactionType type = getTransactionType(address, vinList);
        BigDecimal value = getTransactionValue(type, address, voutList, divider);
        TransactionStatus status = getTransactionStatus(json.optInt("confirmations"));
        Date date = new Date(json.optLong("blockTime") * 1000);

        return new TransactionDTO(index, txId, value, status, type, date);
    }

    public static TransactionResponseDTO composeBinance(TransactionPage page, String address, Integer startIndex, Integer limit) {
        TransactionResponseDTO result = new TransactionResponseDTO();
        List<TransactionDTO> transactions = new ArrayList<>();

        for (int i = 0; i < page.getTx().size(); i++) {
            if ((i + 1 < startIndex)) {
                continue;
            }

            com.binance.dex.api.client.domain.Transaction transaction = page.getTx().get(i);

            String txId = transaction.getTxHash();
            TransactionType type = transaction.getToAddr().equalsIgnoreCase(address) ? TransactionType.DEPOSIT : TransactionType.WITHDRAW;
            BigDecimal value = new BigDecimal(transaction.getValue()).stripTrailingZeros();
            TransactionStatus status = TransactionStatus.COMPLETE;
            Date date = Date.from(ZonedDateTime.parse(transaction.getTimeStamp()).toInstant());

            transactions.add(new TransactionDTO(startIndex + i, txId, value, status, type, date));

            if ((startIndex + limit) == (i + 1)) {
                break;
            }
        }

        result.setTotal(page.getTotal().intValue());
        result.setTransactions(transactions);

        return result;
    }

    public static TransactionResponseDTO composeRippled(JSONArray transactionsArray, String address, Long divider, Integer startIndex, Integer limit) {
        TransactionResponseDTO result = new TransactionResponseDTO();
        List<TransactionDTO> transactions = new ArrayList<>();

        int count = 0;
        int k = 0;

        for (int i = 0; i < transactionsArray.size(); i++) {
            count++;

            if ((i + 1 < startIndex) || ((startIndex + limit) == (i + 1))) {
                continue;
            }

            JSONObject txs = transactionsArray.getJSONObject(i);
            String transactionResult = txs.optJSONObject("meta").optString("TransactionResult");
            TransactionStatus status = getRippledTransactionStatus(transactionResult);
            JSONObject tx = txs.optJSONObject("tx");

            String txId = tx.optString("hash");
            TransactionType type = tx.optString("Account").equalsIgnoreCase(address) ? TransactionType.WITHDRAW : TransactionType.DEPOSIT;
            BigDecimal value = new BigDecimal(tx.optString("Amount")).divide(BigDecimal.valueOf(divider)).stripTrailingZeros();
            Date date = new Date((tx.optLong("date") + 946684800L) * 1000);

            transactions.add(new TransactionDTO(startIndex + k, txId, value, status, type, date));
            k++;
        }

        result.setTotal(count);
        result.setTransactions(transactions);

        return result;
    }

    public static TransactionResponseDTO composeTrongrid(JSONArray transactionsArray, String address, Long divider, Integer startIndex, Integer limit) {
        TransactionResponseDTO result = new TransactionResponseDTO();

        List<TransactionDTO> transactions = new ArrayList<>();

        int count = 0;
        int k = 0;

        for (int i = 0; i < transactionsArray.size(); i++) {
            JSONObject tx = transactionsArray.getJSONObject(i);

            JSONObject rowData = tx.optJSONObject("raw_data").optJSONArray("contract").getJSONObject(0).getJSONObject("parameter").optJSONObject("value");

            if (rowData.containsKey("asset_name")) {
                continue;
            }

            count++;

            if ((i + 1 < startIndex) || ((startIndex + limit) == (i + 1))) {
                continue;
            }

            String txId = tx.optString("txID");
            Long blockTimestamp = tx.optLong("block_timestamp");
            Long amount = rowData.optLong("amount");
            String ownerAddress = Base58.toBase58(rowData.optString("owner_address")).toLowerCase();
            String code = tx.optJSONArray("ret").getJSONObject(0).optString("contractRet");

            TransactionType type = ownerAddress.equalsIgnoreCase(address) ? TransactionType.WITHDRAW : TransactionType.DEPOSIT;
            BigDecimal value = BigDecimal.valueOf(amount).divide(BigDecimal.valueOf(divider)).stripTrailingZeros();
            TransactionStatus status = code.equalsIgnoreCase("SUCCESS") ? TransactionStatus.COMPLETE : TransactionStatus.PENDING;

            Date date = new Date(blockTimestamp);

            transactions.add(new TransactionDTO(startIndex + k, txId, value, status, type, date));

            k++;
        }

        result.setTotal(count);
        result.setTransactions(transactions);

        return result;
    }

    public static TransactionStatus getRippledTransactionStatus(String str) {
        if (str.equalsIgnoreCase("tesSUCCESS")) {
            return TransactionStatus.COMPLETE;
        }

        return TransactionStatus.FAIL;
    }

    private static BigDecimal getTransactionValue(String value, Long divider) {
        if (value != null && value.length() > 0) {
            return new BigDecimal(value).divide(BigDecimal.valueOf(divider)).stripTrailingZeros();
        }

        return null;
    }

    private static TransactionStatus getTransactionStatus(Integer confirmations) {
        return (confirmations == null || confirmations < 3) ? TransactionStatus.PENDING : TransactionStatus.COMPLETE;
    }

    private static TransactionType getTransactionType(String address, List<JSONObject> vinList) {
        return vinList.stream().anyMatch(e -> e.getJSONArray("addresses").toString().toLowerCase().contains(address.toLowerCase())) ? TransactionType.WITHDRAW : TransactionType.DEPOSIT;
    }

    private static BigDecimal getTransactionValue(TransactionType type, String address, List<JSONObject> voutList, Long divider) {
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