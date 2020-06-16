package com.batm.util;

import com.batm.dto.TransactionDetailsDTO;
import com.batm.dto.TransactionListDTO;
import com.batm.dto.TxListDTO;
import com.batm.entity.BaseTxEntity;
import com.batm.entity.TransactionRecord;
import com.batm.model.TransactionGroupType;
import com.batm.model.TransactionStatus;
import com.batm.model.TransactionType;
import org.apache.commons.lang.StringUtils;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class TxUtil {

    public static TransactionListDTO buildTxs(Map<String, TransactionDetailsDTO> map, Integer startIndex, Integer limit, TxListDTO txDTO) {
        mergeGroupGroupTxs(map, txDTO.getGiftList(), TransactionGroupType.GIFT);
        mergeGroupGroupTxs(map, txDTO.getC2cList(), TransactionGroupType.EXCHANGE);
        mergeBuySellTxs(map, txDTO.getBuySellList());
        List<TransactionDetailsDTO> list = convertAndSort(map);

        TransactionListDTO dto = new TransactionListDTO();
        dto.setTotal(list.size());
        dto.setTransactions(list.subList(startIndex - 1, Math.min(list.size(), startIndex + limit - 1)));

        return dto;
    }

    private static void mergeGroupGroupTxs(Map<String, TransactionDetailsDTO> map, List<? extends BaseTxEntity> list, TransactionGroupType group) {
        if (list != null && !list.isEmpty()) {
            list.stream().forEach(e -> {
                if (map.containsKey(e.getTxId())) {
                    TransactionType type = map.get(e.getTxId()).getType();
                    map.get(e.getTxId()).setType(TransactionType.convert(type, group));
                }
            });
        }
    }

    private static void mergeBuySellTxs(Map<String, TransactionDetailsDTO> map, List<TransactionRecord> txList) {
        if (txList != null && !txList.isEmpty()) {
            txList.stream().forEach(e -> {
                TransactionType type = e.getTransactionType();
                TransactionStatus status = e.getTransactionStatus(type);

                if (StringUtils.isNotBlank(e.getDetail()) && map.containsKey(e.getDetail())) {
                    map.get(e.getDetail()).setType(type);
                    map.get(e.getDetail()).setStatus(status);
                } else {
                    TransactionDetailsDTO transactionDetailsDTO = new TransactionDetailsDTO(null, Util.format6(e.getCryptoAmount()), type, status, e.getServerTime());
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