package com.batm.dto.mapper;

import com.batm.dto.*;
import com.batm.util.Base58;
import com.batm.util.Util;
import com.binance.dex.api.client.domain.Transaction;
import com.binance.dex.api.client.domain.TransactionPage;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.springframework.stereotype.Component;

import javax.xml.bind.DatatypeConverter;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class TransactionMapper {

    public static BlockbookTransactionDTO toBlockbookTransactionDTO(JSONObject jsonObject, String address, Long blockbookCoinDivider) {
        List<JSONObject> vinJsonObjectList = Util.jsonArrayToList(jsonObject.optJSONArray("vin"));
        List<JSONObject> voutJsonObjectList = Util.jsonArrayToList(jsonObject.optJSONArray("vout"));

        return new BlockbookTransactionDTO()
                .setAddress(address)
                .setTxid(jsonObject.optString("txid"))
                .setVersion(jsonObject.optInt("version"))
                .setBlockHash(jsonObject.optString("blockHash"))
                .setBlockHeight(jsonObject.optInt("blockHeight"))
                .setConfirmations(jsonObject.optInt("confirmations"))
                .setBlockTime(jsonObject.optLong("blockTime"))
                .setValue(TransactionMapper.getBigDecimalFromStringWithDividing(jsonObject.optString("value"), blockbookCoinDivider))
                .setValueIn(TransactionMapper.getBigDecimalFromStringWithDividing(jsonObject.optString("valueIn"), blockbookCoinDivider))
                .setFees(TransactionMapper.getBigDecimalFromStringWithDividing(jsonObject.optString("fees"), blockbookCoinDivider))
                .setHex(jsonObject.optString("hex"))
                .setVin(vinJsonObjectList.stream().map(vin -> TransactionMapper.getBlockbookTransactionVinDTO(vin, blockbookCoinDivider)).collect(Collectors.toList()))
                .setVout(voutJsonObjectList.stream().map(vout -> TransactionMapper.getBlockbookTransactionVoutDTO(vout, blockbookCoinDivider)).collect(Collectors.toList()));
    }

    public static RippledTransactionDTO toRippledTransactionDTO(JSONObject jsonObject, Long rippledCoinDivider) {
        final long MILLENNIUM_IN_SECONDS = 946684800;
        JSONObject tx = jsonObject.optJSONObject("tx");
        Boolean verified = jsonObject.optBoolean("validated");

        return new RippledTransactionDTO()
                .setAccount(tx.optString("Account"))
                .setDestination(tx.optString("Destination"))
                .setDate(tx.optLong("date") + MILLENNIUM_IN_SECONDS)
                .setAmount(TransactionMapper.getBigDecimalFromStringWithDividing(tx.optString("Amount"), rippledCoinDivider))
                .setHash(tx.optString("hash"))
                .setVerified(verified);
    }

    // todo complete
    public static TrongridTransactionDTO toTrongridTransactionDTO(JSONObject jsonObject, String address, Long trongridDivider) {
        try {
            String hexAddress = TransactionMapper.getHexStringFromBase58(address).toLowerCase();
            String txId = jsonObject.optString("tx_id") != null && !jsonObject.optString("tx_id").equals("")
                    ? jsonObject.optString("tx_id") : jsonObject.optString("txID");
            JSONArray contract = jsonObject.optJSONObject("raw_data").optJSONArray("contract");
            List<JSONObject> contractList = Util.jsonArrayToList(contract);
            List<JSONObject> ret = Util.jsonArrayToList(jsonObject.optJSONArray("ret"));
            JSONObject parameter = null;

            for (int i = 0; i < contractList.size(); i++) {
                JSONObject p = contractList.get(i).optJSONObject("parameter");
                JSONObject value = p.optJSONObject("value");
                String owner = value.optString("owner_address");
                String to = value.optString("to_address");
                long amount = value.optLong("amount");

                if (amount > 0
                        && (value.optString("asset_name") == null || value.optString("asset_name").equals(""))
                        && (!owner.equals("") && !to.equals("") && !owner.equals(to))
                        && (owner.equals(hexAddress.substring(0, owner.length())) || to.equals(hexAddress.substring(0, to.length())))) {
                    parameter = value;
                    break;
                }
            }

            if (parameter != null) {
                return new TrongridTransactionDTO()
                        .setBlockTimestamp(jsonObject.optLong("block_timestamp"))
                        .setTxID(txId)
                        .setAmount(TransactionMapper.getBigDecimalFromStringWithDividing(Long.toString(parameter.optLong("amount")), trongridDivider))
                        .setToAddress(parameter.optString("to_address"))
                        .setOwnerAddress(parameter.optString("owner_address"))
                        .setCode(ret.get(0).optString("code"));
            }
        } catch (Exception e) {
            return null;
        }

        return null;
    }

    public static TransactionDTO toTransactionDTO(BlockbookTransactionDTO blockbookTransactionDTO) {
        return new TransactionDTO()
                .setTxid(blockbookTransactionDTO.getTxid())
                .setValue(blockbookTransactionDTO.getValue())
                .setDate(new Date(blockbookTransactionDTO.getBlockTime() * 1000))
                .setType(TransactionMapper.getBlockbookTransactionType(blockbookTransactionDTO))
                .setStatus(TransactionMapper.getTransactionStatusByConfirmations(blockbookTransactionDTO.getConfirmations()));
    }

    public static TransactionDTO toTransactionDTO(Transaction transaction, String address) {
        return new TransactionDTO()
                .setTxid(transaction.getTxHash())
                .setValue(new BigDecimal(transaction.getValue()))
                .setDate(Date.from(ZonedDateTime.parse(transaction.getTimeStamp()).toInstant()))
                .setType(transaction.getToAddr().equals(address)
                        ? TransactionDTO.TransactionType.DEPOSIT : TransactionDTO.TransactionType.WITHDRAW)
                .setStatus(TransactionDTO.TransactionStatus.COMPLETE);
    }

    public static TransactionDTO toTransactionDTO(RippledTransactionDTO transaction, String address) {
        return new TransactionDTO()
                .setTxid(transaction.getHash())
                .setValue(transaction.getAmount())
                .setDate(new Date(transaction.getDate() * 1000))
                .setType(transaction.getDestination().equals(address)
                        ? TransactionDTO.TransactionType.DEPOSIT : TransactionDTO.TransactionType.WITHDRAW)
                .setStatus(transaction.getVerified() ? TransactionDTO.TransactionStatus.COMPLETE
                        : TransactionDTO.TransactionStatus.PENDING);
    }

    public static TransactionDTO toTransactionDTO(TrongridTransactionDTO transaction, String address) {
        return new TransactionDTO()
                .setTxid(transaction.getTxID())
                .setStatus(transaction.getCode().equals("SUCESS") ? TransactionDTO.TransactionStatus.COMPLETE : TransactionDTO.TransactionStatus.PENDING)
                .setType(transaction.getToAddress().equals(address) ? TransactionDTO.TransactionType.DEPOSIT : TransactionDTO.TransactionType.WITHDRAW)
                .setValue(transaction.getAmount())
                .setDate(new Date(transaction.getBlockTimestamp()));
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

    public static TransactionResponseDTO<TransactionDTO> toTransactionResponseDTO(TransactionPage transactionPage, String address) {
        List<TransactionDTO> transactionDTOList = new ArrayList<>();
        List<Transaction> binanceTransactions = transactionPage.getTx();

        for (int i = 0; i < binanceTransactions.size(); i++) {
            TransactionDTO transactionDTO = TransactionMapper.toTransactionDTO(binanceTransactions.get(i), address);
            transactionDTO.setIndex(i + 1);
            transactionDTOList.add(transactionDTO);
        }

        return new TransactionResponseDTO<TransactionDTO>()
                .setAddress(address)
                .setTxs(transactionPage.getTotal())
                .setTransactions(transactionDTOList);
    }

    public static TransactionResponseDTO<TransactionDTO> toTransactionResponseDTO(List<RippledTransactionDTO> rippledTransactionDTOS, String address) {
        List<TransactionDTO> transactionDTOList = new ArrayList<>();

        for (int i = 0; i < rippledTransactionDTOS.size(); i++) {
            TransactionDTO transactionDTO = TransactionMapper.toTransactionDTO(rippledTransactionDTOS.get(i), address);
            transactionDTO.setIndex(i + 1);
            transactionDTOList.add(transactionDTO);
        }

        return new TransactionResponseDTO<TransactionDTO>()
                .setAddress(address)
                .setTxs((long) rippledTransactionDTOS.size())
                .setTransactions(transactionDTOList);
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
                .setAddresses(Util.jsonArrayToList(jsonObject.optJSONArray("addresses")));
    }

    private static BlockbookTransactionVoutDTO getBlockbookTransactionVoutDTO(JSONObject jsonObject, Long blockbookCoinDivider) {
        return new BlockbookTransactionVoutDTO()
                .setValue(TransactionMapper.getBigDecimalFromStringWithDividing(jsonObject.optString("value"), blockbookCoinDivider))
                .setN(jsonObject.optInt("n"))
                .setHex(jsonObject.optString("hex"))
                .setIsAddress(jsonObject.optBoolean("isAddress"))
                .setSpent(jsonObject.optBoolean("spent"))
                .setAddresses(Util.jsonArrayToList(jsonObject.optJSONArray("addresses")));
    }

    private static BigDecimal getBigDecimalFromStringWithDividing(String value, Long divider) {
        if (value != null && value.length() > 0) {
            return new BigDecimal(value).divide(BigDecimal.valueOf(divider)).stripTrailingZeros();
        }
        return null;
    }

    private static TransactionDTO.TransactionStatus getTransactionStatusByConfirmations(Integer confirmations) {
        return confirmations == null || confirmations < 3
                ? TransactionDTO.TransactionStatus.PENDING
                : TransactionDTO.TransactionStatus.COMPLETE;
    }

    private static TransactionDTO.TransactionType getBlockbookTransactionType(BlockbookTransactionDTO blockbookTransactionDTO) {
        List<BlockbookTransactionVinDTO> vins = blockbookTransactionDTO.getVin();
        return vins.stream().anyMatch(vin -> vin.getAddresses().contains(blockbookTransactionDTO.getAddress()))
                ? TransactionDTO.TransactionType.WITHDRAW : TransactionDTO.TransactionType.DEPOSIT;
    }

    private static String getHexStringFromBase58(String base58) {
        return DatatypeConverter.printHexBinary(Base58.decode(base58));
    }
}
