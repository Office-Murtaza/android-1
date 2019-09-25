package com.batm.util;

import com.batm.dto.BlockbookTxDTO;
import com.batm.dto.TransactionDTO;
import com.binance.dex.api.client.domain.TransactionPage;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TransactionUtil {

    public static BlockbookTxDTO composeBlockbook(Integer total, JSONArray transactionsArray, String address, Long divider, Integer startIndex, Integer limit) {
        BlockbookTxDTO result = new BlockbookTxDTO();
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
        TransactionDTO.TransactionType type = getTransactionType(address, vinList);
        BigDecimal value = getTransactionValue(type, address, voutList, divider);
        TransactionDTO.TransactionStatus status = getTransactionStatus(json.optInt("confirmations"));
        Date date = new Date(json.optLong("blockTime") * 1000);

        return new TransactionDTO(index, txId, value, status, type, date);
    }

    public static BlockbookTxDTO composeBinance(TransactionPage page, String address, Integer startIndex, Integer limit) {
        BlockbookTxDTO result = new BlockbookTxDTO();
        List<TransactionDTO> transactions = new ArrayList<>();

        for (int i = 0; i < page.getTx().size(); i++) {
            if ((i + 1 < startIndex)) {
                continue;
            }

            com.binance.dex.api.client.domain.Transaction transaction = page.getTx().get(i);

            String txId = transaction.getTxHash();
            TransactionDTO.TransactionType type = transaction.getToAddr().equalsIgnoreCase(address) ? TransactionDTO.TransactionType.DEPOSIT : TransactionDTO.TransactionType.WITHDRAW;
            BigDecimal value = new BigDecimal(transaction.getValue()).stripTrailingZeros();
            TransactionDTO.TransactionStatus status = TransactionDTO.TransactionStatus.COMPLETE;
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

    public static BlockbookTxDTO composeRippled(JSONArray transactionsArray, String address, Long divider, Integer startIndex, Integer limit) {
        BlockbookTxDTO result = new BlockbookTxDTO();
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

            TransactionDTO.TransactionStatus status = transactionResult.equalsIgnoreCase("tesSUCCESS") ? TransactionDTO.TransactionStatus.COMPLETE : TransactionDTO.TransactionStatus.PENDING;

            JSONObject tx = txs.optJSONObject("tx");

            String txId = tx.optString("hash");
            TransactionDTO.TransactionType type = tx.optString("Account").equalsIgnoreCase(address) ? TransactionDTO.TransactionType.WITHDRAW : TransactionDTO.TransactionType.DEPOSIT;
            BigDecimal value = new BigDecimal(tx.optString("Amount")).divide(BigDecimal.valueOf(divider)).stripTrailingZeros();
            Date date = new Date((tx.optLong("date") + 946684800L) * 1000);

            transactions.add(new TransactionDTO(startIndex + k, txId, value, status, type, date));
            k++;
        }

        result.setTotal(count);
        result.setTransactions(transactions);

        return result;
    }

    public static BlockbookTxDTO composeTrongrid(JSONArray transactionsArray, String address, Long divider, Integer startIndex, Integer limit) {
        BlockbookTxDTO result = new BlockbookTxDTO();

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
            String code = tx.optJSONArray("ret").getJSONObject(0).optString("code");

            TransactionDTO.TransactionType type = ownerAddress.equalsIgnoreCase(address) ? TransactionDTO.TransactionType.WITHDRAW : TransactionDTO.TransactionType.DEPOSIT;
            BigDecimal value = BigDecimal.valueOf(amount).divide(BigDecimal.valueOf(divider)).stripTrailingZeros();
            TransactionDTO.TransactionStatus status = code.equalsIgnoreCase("SUCESS") ? TransactionDTO.TransactionStatus.COMPLETE : TransactionDTO.TransactionStatus.PENDING;

            Date date = new Date(blockTimestamp);

            transactions.add(new TransactionDTO(startIndex + k, txId, value, status, type, date));

            k++;
        }

        result.setTotal(count);
        result.setTransactions(transactions);

        return result;
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