package com.belco.server.util;

import com.belco.server.dto.TxDetailsDTO;
import com.belco.server.dto.TxHistoryDTO;
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

    public static TxHistoryDTO buildTxs(Map<String, TxDetailsDTO> map, Integer startIndex, Integer limit, List<TransactionRecord> transactionRecords, List<TransactionRecordWallet> transactionRecordWallets) {
        mergeTransactionRecords(map, transactionRecords);
        mergeTransactionRecordWallets(map, transactionRecordWallets);

        List<TxDetailsDTO> list = convertAndSort(map);

        return new TxHistoryDTO(list.size(), list.subList(startIndex - 1, Math.min(list.size(), startIndex + limit - 1)));
    }

    private static void mergeTransactionRecordWallets(Map<String, TxDetailsDTO> map, List<TransactionRecordWallet> list) {
        if (list != null && !list.isEmpty()) {
            list.stream().forEach(e -> {
                if (map.containsKey(e.getTxId())) {
                    TransactionType type = map.get(e.getTxId()).getType();
                    map.get(e.getTxId()).setType(TransactionType.convert(type, TransactionType.valueOf(e.getType())));
                }
            });
        }
    }

    private static void mergeTransactionRecords(Map<String, TxDetailsDTO> map, List<TransactionRecord> txList) {
        if (txList != null && !txList.isEmpty()) {
            txList.stream().forEach(e -> {
                TransactionType type = e.getTransactionType();
                TransactionStatus status = e.getTransactionStatus(type);

                if (StringUtils.isNotBlank(e.getDetail()) && map.containsKey(e.getDetail())) {
                    map.get(e.getDetail()).setType(type);
                    map.get(e.getDetail()).setStatus(status);
                } else {
                    TxDetailsDTO txDetailsDTO = new TxDetailsDTO(null, Util.format(e.getCryptoAmount(), 6), type, status, e.getServerTime());
                    String txDbId = e.getId().toString();
                    txDetailsDTO.setTxDbId(txDbId);

                    map.put(txDbId, txDetailsDTO);
                }
            });
        }
    }

    private static List<TxDetailsDTO> convertAndSort(Map<String, TxDetailsDTO> map) {
        if (!map.isEmpty()) {
            List<TxDetailsDTO> list = new ArrayList<>(map.values());
            list.sort(Comparator.comparing(TxDetailsDTO::getDate1).reversed());

            return list;
        }

        return new ArrayList<>();
    }
}