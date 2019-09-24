package com.batm.util;

import com.batm.dto.BlockbookTxDTO;
import com.batm.dto.TransactionDTO;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TrongridUtil {

    public static BlockbookTxDTO compose(JSONArray transactionsArray, String address, Long divider, Integer fromIndex, Integer limit) {
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

            if ((i + 1 < fromIndex) || ((fromIndex + limit) == (i + 1))) {
                continue;
            }

            k++;

            String txId = tx.optString("txID");
            Long blockTimestamp = tx.optLong("block_timestamp");
            Long amount = rowData.optLong("amount");
            String ownerAddress = Base58.toBase58(rowData.optString("owner_address")).toLowerCase();
            String code = tx.optJSONArray("ret").getJSONObject(0).optString("code");

            TransactionDTO.TransactionType type = ownerAddress.equalsIgnoreCase(address) ? TransactionDTO.TransactionType.WITHDRAW : TransactionDTO.TransactionType.DEPOSIT;
            BigDecimal value = BigDecimal.valueOf(amount).divide(BigDecimal.valueOf(divider)).stripTrailingZeros();
            TransactionDTO.TransactionStatus status = code.equalsIgnoreCase("SUCESS") ? TransactionDTO.TransactionStatus.COMPLETE : TransactionDTO.TransactionStatus.PENDING;

            Date date = new Date(blockTimestamp);

            transactions.add(new TransactionDTO(fromIndex + k, txId, value, status, type, date));
        }

        result.setTotal(count);
        result.setTransactions(transactions);

        return result;
    }
}