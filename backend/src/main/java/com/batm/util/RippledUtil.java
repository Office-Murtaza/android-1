package com.batm.util;

import com.batm.dto.BlockbookTxDTO;
import com.batm.dto.TransactionDTO;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RippledUtil {

    public static BlockbookTxDTO compose(JSONArray transactionsArray, String address, Long divider, Integer fromIndex, Integer limit) {
        BlockbookTxDTO result = new BlockbookTxDTO();
        List<TransactionDTO> transactions = new ArrayList<>();

        int count = 0;
        int k = 0;

        for (int i = 0; i < transactionsArray.size(); i++) {
            count++;

            if ((i + 1 < fromIndex) || ((fromIndex + limit) == (i + 1))) {
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

            transactions.add(new TransactionDTO(fromIndex + k, txId, value, status, type, date));
            k++;
        }

        result.setTotal(count);
        result.setTransactions(transactions);

        return result;
    }
}
