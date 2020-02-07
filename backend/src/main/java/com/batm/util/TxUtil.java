package com.batm.util;

import com.batm.dto.TransactionDTO;
import com.batm.dto.TransactionListDTO;
import com.batm.entity.TransactionRecord;
import com.batm.entity.TransactionRecordGift;
import com.batm.model.TransactionStatus;
import com.batm.model.TransactionType;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class TxUtil {

    public static TransactionListDTO buildTxs(Map<String, TransactionDTO> map, Integer startIndex, Integer limit, List<TransactionRecordGift> giftList, List<TransactionRecord> txList) {
        TxUtil.mergeGifts(map, giftList);
        TxUtil.mergeTxs(map, txList);
        List<TransactionDTO> list = TxUtil.convertAndSort(map);

        return TxUtil.build(list, startIndex, limit);
    }

    private static void mergeGifts(Map<String, TransactionDTO> map, List<TransactionRecordGift> giftList) {
        if (giftList != null && !giftList.isEmpty()) {
            giftList.stream().forEach(e -> {
                if (map.containsKey(e.getTxId())) {
                    TransactionType type = map.get(e.getTxId()).getType();
                    map.get(e.getTxId()).setType(TransactionType.getGiftType(type));
                }
            });
        }
    }

    private static void mergeTxs(Map<String, TransactionDTO> map, List<TransactionRecord> txList) {
        if (txList != null && !txList.isEmpty()) {
            txList.stream().forEach(e -> {
                TransactionType type = e.getTransactionType();
                TransactionStatus status = e.getTransactionStatus(type);

                if (StringUtils.isNotBlank(e.getDetail()) && map.containsKey(e.getDetail())) {
                    map.get(e.getDetail()).setType(type);
                    map.get(e.getDetail()).setStatus(status);
                } else {
                    TransactionDTO transactionDTO = new TransactionDTO(null, Util.format6(e.getCryptoAmount()), type, status, e.getServerTime());
                    String txDbId = e.getId().toString();
                    transactionDTO.setTxDbId(txDbId);

                    map.put(txDbId, transactionDTO);
                }
            });
        }
    }

    private static List<TransactionDTO> convertAndSort(Map<String, TransactionDTO> map) {
        if (!map.isEmpty()) {
            List<TransactionDTO> list = new ArrayList<>(map.values());
            list.sort(Comparator.comparing(TransactionDTO::getDate1).reversed());

            return list;
        }

        return new ArrayList<>();
    }

    private static TransactionListDTO build(List<TransactionDTO> list, Integer startIndex, Integer limit) {
        TransactionListDTO result = new TransactionListDTO();
        List<TransactionDTO> transactions = new ArrayList<>();
        int index = startIndex - 1;

        for (int i = 0; i < list.size(); i++) {
            if (index > i) {
                continue;
            }

            TransactionDTO dto = list.get(i);
            dto.setIndex(i + 1);
            transactions.add(dto);

            if (index + limit == i + 1) {
                break;
            }
        }

        result.setTotal(list.size());
        result.setTransactions(transactions);

        return result;
    }
}