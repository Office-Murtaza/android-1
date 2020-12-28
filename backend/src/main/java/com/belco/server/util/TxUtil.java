package com.belco.server.util;

import com.belco.server.dto.TransactionDetailsDTO;
import com.belco.server.dto.TransactionHistoryDTO;
import com.belco.server.entity.TransactionRecord;
import com.belco.server.entity.TransactionRecordWallet;
import com.belco.server.model.TransactionStatus;
import com.belco.server.model.TransactionType;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class TxUtil {

    public static TransactionHistoryDTO buildTxs(Map<String, TransactionDetailsDTO> map, Integer startIndex, Integer limit, List<TransactionRecord> transactionRecords, List<TransactionRecordWallet> transactionRecordWallets) {
        mergeTransactionRecords(map, transactionRecords);
        mergeTransactionRecordWallets(map, transactionRecordWallets);

        List<TransactionDetailsDTO> list = convertAndSort(map);

        return new TransactionHistoryDTO(list.size(), list.subList(startIndex - 1, Math.min(list.size(), startIndex + limit - 1)));
    }

    private static void mergeTransactionRecordWallets(Map<String, TransactionDetailsDTO> map, List<TransactionRecordWallet> list) {
        if (list != null && !list.isEmpty()) {
            list.stream().forEach(e -> {
                if (map.containsKey(e.getTxId())) {
                    TransactionType type = map.get(e.getTxId()).getType();
                    map.get(e.getTxId()).setType(TransactionType.convert(type, TransactionType.valueOf(e.getType())));
                }
            });
        }
    }

    private static void mergeTransactionRecords(Map<String, TransactionDetailsDTO> map, List<TransactionRecord> txList) {
        if (txList != null && !txList.isEmpty()) {
            txList.stream().forEach(e -> {
                TransactionType type = e.getTransactionType();
                TransactionStatus status = e.getTransactionStatus(type);

                if (StringUtils.isNotBlank(e.getDetail()) && map.containsKey(e.getDetail())) {
                    map.get(e.getDetail()).setType(type);
                    map.get(e.getDetail()).setStatus(status);
                } else {
                    TransactionDetailsDTO transactionDetailsDTO = new TransactionDetailsDTO(null, Util.format(e.getCryptoAmount(), 6), type, status, e.getServerTime());
                    String txDbId = e.getId().toString();
                    transactionDetailsDTO.setTxDbId(txDbId);

                    map.put(txDbId, transactionDetailsDTO);
                }
            });
        }
    }

    private static List<TransactionDetailsDTO> convertAndSort(Map<String, TransactionDetailsDTO> map) {
        if (!map.isEmpty()) {
            List<TransactionDetailsDTO> list = new ArrayList<>(map.values());
            list.sort(Comparator.comparing(TransactionDetailsDTO::getDate1).reversed());

            return list;
        }

        return new ArrayList<>();
    }
}