package com.batm.dto.mapper;

import com.batm.dto.*;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class TransactionMapper {

    public static BlockbookTransactionDTO toBlockbookTransactionDTO(JSONObject jsonObject, String address, Long blockbookCoinDivider) {
        List<JSONObject> vinJsonObjectList = TransactionMapper.toList(jsonObject.optJSONArray("vin"));
        List<JSONObject> voutJsonObjectList = TransactionMapper.toList(jsonObject.optJSONArray("vout"));

        return new BlockbookTransactionDTO()
                .setAddress(address)
                .setTxid(jsonObject.optString("txid"))
                .setVersion(jsonObject.optInt("version"))
                .setBlockHash(jsonObject.optString("blockHash"))
                .setBlockHeight(jsonObject.optInt("blockHeight"))
                .setConfirmations(jsonObject.optInt("confirmations"))
                .setBlockTime(jsonObject.optLong("blockTime"))
                .setValue(TransactionMapper.getBigDecimalFromStringWithDividing(jsonObject.optString("value"), blockbookCoinDivider))
                .setValue(TransactionMapper.getBigDecimalFromStringWithDividing(jsonObject.optString("valueIn"), blockbookCoinDivider))
                .setValue(TransactionMapper.getBigDecimalFromStringWithDividing(jsonObject.optString("fees"), blockbookCoinDivider))
                .setHex(jsonObject.optString("hex"))
                .setVin(vinJsonObjectList.stream().map(vin -> TransactionMapper.getBlockbookTransactionVinDTO(vin, blockbookCoinDivider)).collect(Collectors.toList()))
                .setVout(voutJsonObjectList.stream().map(vout -> TransactionMapper.getBlockbookTransactionVoutDTO(vout, blockbookCoinDivider)).collect(Collectors.toList()));
    }

    public static TransactionDTO toTransactionDTO(BlockbookTransactionDTO blockbookTransactionDTO) {
        return new TransactionDTO()
                .setTxid(blockbookTransactionDTO.getTxid())
                .setValue(blockbookTransactionDTO.getValue())
                .setDate(new Date(blockbookTransactionDTO.getBlockTime() * 1000))
                .setType(TransactionMapper.getTransactionType(blockbookTransactionDTO))
                .setStatus(TransactionMapper.getTransactionStatusByConfirmations(blockbookTransactionDTO.getConfirmations()));
    }

    public static TransactionResponseDTO<TransactionDTO> toTransactionResponseDTO(TransactionResponseDTO<BlockbookTransactionDTO> transactionDTOTransactionResponseDTO) {
        List<TransactionDTO> transactionDTOList = new ArrayList<>();
        List<BlockbookTransactionDTO> blockbookTransactionDTOList = transactionDTOTransactionResponseDTO.getTransactions();

        for (int i = 0; i < blockbookTransactionDTOList.size(); i++) {
            TransactionDTO transactionDTO = TransactionMapper.toTransactionDTO(blockbookTransactionDTOList.get(i));
            transactionDTO.setIndex(i + 1);
            transactionDTOList.add(transactionDTO);
        }

        return new TransactionResponseDTO<TransactionDTO>(
                transactionDTOTransactionResponseDTO.getTotalPages(),
                transactionDTOTransactionResponseDTO.getItemsOnPage(),
                transactionDTOTransactionResponseDTO.getAddress(),
                transactionDTOTransactionResponseDTO.getTxs(),
                transactionDTOList
        );
    }

    private static BlockbookTransactionVinDTO getBlockbookTransactionVinDTO(JSONObject jsonObject, Long blockbookCoinDivider) {
        return new BlockbookTransactionVinDTO()
                .setTxid(jsonObject.optString("txid"))
                .setSequence(jsonObject.optLong("sequence"))
                .setN(jsonObject.optInt("n"))
                .setIsAddress(jsonObject.optBoolean("isAddress"))
                .setValue(TransactionMapper.getBigDecimalFromStringWithDividing(jsonObject.optString("value"), blockbookCoinDivider))
                .setHex(jsonObject.optString("hex"))
                .setVout(jsonObject.optInt("vout"))
                .setAddresses(TransactionMapper.toList(jsonObject.optJSONArray("addresses")));
    }

    private static BlockbookTransactionVoutDTO getBlockbookTransactionVoutDTO(JSONObject jsonObject, Long blockbookCoinDivider) {
        return new BlockbookTransactionVoutDTO()
                .setValue(TransactionMapper.getBigDecimalFromStringWithDividing(jsonObject.optString("value"), blockbookCoinDivider))
                .setN(jsonObject.optInt("n"))
                .setHex(jsonObject.optString("hex"))
                .setIsAddress(jsonObject.optBoolean("isAddress"))
                .setSpent(jsonObject.optBoolean("spent"))
                .setAddresses(TransactionMapper.toList(jsonObject.optJSONArray("addresses")));
    }

    private static <T> List<T> toList(JSONArray jsonArray) {
        List<T> list = new ArrayList<>();
        if (jsonArray != null) {
            int len = jsonArray.size();
            for (int i = 0; i < len; i++) {
                list.add((T) jsonArray.opt(i));
            }
        }
        return list;
    }

    private static BigDecimal getBigDecimalFromStringWithDividing(String value, Long divider) {
        if (value != null && value.length() > 0) {
            return new BigDecimal(value).divide(BigDecimal.valueOf(divider)).stripTrailingZeros();
        }
        return null;
    }

    /*
        unknown(0),
        pending(1), confirmation null, 0, 1, 2
        complete(2), 3<
        fail(3)
     */
    private static Integer getTransactionStatusByConfirmations(Integer confirmations) {
        return confirmations == null || confirmations < 3 ? 1 : 2;
    }

    /*
        deposit(1),
        withdraw(2),
        send gift(3),
        receive gift(4),
        buy(5),
        sell(6)
     */
    private static Integer getTransactionType(BlockbookTransactionDTO blockbookTransactionDTO) {
        List<BlockbookTransactionVinDTO> vins = blockbookTransactionDTO.getVin();
        return vins.stream().anyMatch(vin -> vin.getAddresses().contains(blockbookTransactionDTO.getAddress())) ? 2 : 1;
    }
}
