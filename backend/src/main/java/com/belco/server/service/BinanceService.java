package com.belco.server.service;

import com.belco.server.dto.CurrentAccountDTO;
import com.belco.server.dto.TransactionDetailsDTO;
import com.belco.server.dto.TransactionHistoryDTO;
import com.belco.server.entity.TransactionRecord;
import com.belco.server.model.TransactionStatus;
import com.belco.server.util.Util;
import com.binance.dex.api.client.BinanceDexApiRestClient;
import com.binance.dex.api.client.domain.Account;
import com.binance.dex.api.client.domain.TransactionPage;
import com.binance.dex.api.client.domain.TransactionType;
import com.binance.dex.api.client.domain.request.TransactionsRequest;
import com.google.protobuf.ByteString;
import lombok.Getter;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.web3j.utils.Numeric;
import wallet.core.java.AnySigner;
import wallet.core.jni.AnyAddress;
import wallet.core.jni.CoinType;
import wallet.core.jni.PrivateKey;
import wallet.core.jni.proto.Binance;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Getter
@Service
public class BinanceService {

    private static final String CHAIN_ID = "Binance-Chain-Tigris";
    private static final BigDecimal DIVIDER = BigDecimal.valueOf(100_000_000L);
    private static final BigDecimal FEE = new BigDecimal("0.000375");
    private static final CoinType COIN_TYPE = CoinType.BINANCE;

    private final BinanceDexApiRestClient binanceDex;
    private final RestTemplate rest;
    private final MongoOperations mongo;
    private final WalletService walletService;
    private final NodeService nodeService;

    public BinanceService(BinanceDexApiRestClient binanceDex, RestTemplate rest, MongoOperations mongo, WalletService walletService, NodeService nodeService) {
        this.binanceDex = binanceDex;
        this.mongo = mongo;
        this.rest = rest;
        this.walletService = walletService;
        this.nodeService = nodeService;
    }

    public BigDecimal getBalance(String address) {
        try {
            return Util.format(binanceDex
                    .getAccount(address)
                    .getBalances()
                    .stream()
                    .filter(e -> "BNB".equals(e.getSymbol()))
                    .map(it -> new BigDecimal(it.getFree()).add(new BigDecimal(it.getLocked())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add), 6);
        } catch (Exception e) {
        }

        return BigDecimal.ZERO;
    }

    public BigDecimal getTxFee() {
        return FEE;
    }

    public String submitTransaction(String hex) {
        if (nodeService.isNodeAvailable(COIN_TYPE)) {
            try {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.TEXT_PLAIN);

                JSONObject res = JSONArray.fromObject(rest.postForObject(nodeService.getNodeUrl(COIN_TYPE) + "/api/v1/broadcast", hex, String.class)).getJSONObject(0);

                return res.optString("hash");
            } catch (Exception e) {
                e.printStackTrace();

                if (nodeService.switchToReserveNode(COIN_TYPE)) {
                    return submitTransaction(hex);
                }
            }
        }

        return null;
    }

    public boolean isTransactionSeenOnBlockchain(String txId) {
        if (nodeService.isNodeAvailable(COIN_TYPE)) {
            try {
                return rest.getForObject(nodeService.getNodeUrl(COIN_TYPE) + "/api/v1/tx/" + txId + "?format=json", JSONObject.class) != null;
            } catch (HttpClientErrorException.NotFound notFound) {
                return false;
            } catch (Exception e) {
                e.printStackTrace();

                if (nodeService.switchToReserveNode(COIN_TYPE)) {
                    return isTransactionSeenOnBlockchain(txId);
                }
            }
        }

        return false;
    }

    public TransactionDetailsDTO getTransactionDetails(String txId, String address, String explorerUrl) {
        TransactionDetailsDTO dto = new TransactionDetailsDTO();

        if (nodeService.isNodeAvailable(COIN_TYPE)) {
            try {
                JSONObject res = rest.getForObject(nodeService.getNodeUrl(COIN_TYPE) + "/api/v1/tx/" + txId + "?format=json", JSONObject.class);
                JSONObject msg = res.optJSONObject("tx").optJSONObject("value").optJSONArray("msg").getJSONObject(0);

                dto.setTxId(txId);
                dto.setLink(explorerUrl + "/" + txId);
                dto.setFromAddress(msg.optJSONObject("value").optJSONArray("inputs").getJSONObject(0).optString("address"));
                dto.setToAddress(msg.optJSONObject("value").optJSONArray("outputs").getJSONObject(0).optString("address"));

                com.belco.server.model.TransactionType type = com.belco.server.model.TransactionType.getType(dto.getFromAddress(), dto.getToAddress(), address);
                if (type != null) {
                    dto.setType(type.getValue());
                }

                dto.setStatus(getStatus(res.getInt("code")).getValue());
                dto.setCryptoAmount(getAmount(msg.optJSONObject("value").optJSONArray("inputs").getJSONObject(0).getJSONArray("coins").getJSONObject(0).optString("amount")));
                dto.setCryptoFee(getAmount("1000000"));
            } catch (Exception e) {
                e.printStackTrace();

                if (nodeService.switchToReserveNode(COIN_TYPE)) {
                    return getTransactionDetails(txId, address, explorerUrl);
                }

                dto.setStatus(TransactionStatus.FAIL.getValue());
            }
        }

        return dto;
    }

    public Map<String, TransactionDetailsDTO> getNodeTransactions(String address) {
        try {
            TransactionsRequest request = new TransactionsRequest();
            request.setStartTime(new SimpleDateFormat("yyyy").parse("2010").getTime());
            request.setEndTime(new Date().getTime() + TimeUnit.DAYS.toMillis(1));
            request.setTxType(TransactionType.TRANSFER);
            request.setAddress(address);
            request.setTxAsset("BNB");
            request.setLimit(1000);

            TransactionPage page = binanceDex.getTransactions(request);

            return collectNodeTxs(page, address);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Collections.emptyMap();
    }

    public TransactionHistoryDTO getTransactionHistory(String address, List<TransactionRecord> transactionRecords, List<TransactionDetailsDTO> details) {
        return TransactionService.buildTxs(getNodeTransactions(address), transactionRecords, details);
    }

    public CurrentAccountDTO getCurrentAccount(String address) {
        try {
            Account account = binanceDex.getAccount(address);

            return new CurrentAccountDTO(account.getAccountNumber(), account.getSequence().intValue(), CHAIN_ID);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new CurrentAccountDTO();
    }

    public String sign(Long walletId, String fromAddress, String toAddress, BigDecimal amount) {
        try {
            PrivateKey privateKey;

            if (walletService.isServerAddress(fromAddress)) {
                privateKey = walletService.get(walletId).getCoins().get(CoinType.BINANCE).getPrivateKey();
            } else {
                String path = walletService.getPath(fromAddress);
                privateKey = walletService.get(walletId).getWallet().getKey(CoinType.BINANCE, path);
            }

            CurrentAccountDTO currentDTO = getCurrentAccount(fromAddress);

            Binance.SigningInput.Builder builder = Binance.SigningInput.newBuilder();
            builder.setChainId(currentDTO.getChainId());
            builder.setAccountNumber(currentDTO.getAccountNumber());
            builder.setSequence(currentDTO.getSequence());
            builder.setPrivateKey(ByteString.copyFrom(privateKey.data()));

            Binance.SendOrder.Token.Builder token = Binance.SendOrder.Token.newBuilder();
            token.setDenom("BNB");
            token.setAmount(amount.multiply(DIVIDER).longValue());

            Binance.SendOrder.Input.Builder input = Binance.SendOrder.Input.newBuilder();
            input.setAddress(ByteString.copyFrom(new AnyAddress(fromAddress, CoinType.BINANCE).data()));
            input.addAllCoins(Arrays.asList(token.build()));

            Binance.SendOrder.Output.Builder output = Binance.SendOrder.Output.newBuilder();
            output.setAddress(ByteString.copyFrom(new AnyAddress(toAddress, CoinType.BINANCE).data()));
            output.addAllCoins(Arrays.asList(token.build()));

            Binance.SendOrder.Builder sendOrder = Binance.SendOrder.newBuilder();
            sendOrder.addAllInputs(Arrays.asList(input.build()));
            sendOrder.addAllOutputs(Arrays.asList(output.build()));

            builder.setSendOrder(sendOrder.build());

            Binance.SigningOutput sign = AnySigner.sign(builder.build(), CoinType.BINANCE, Binance.SigningOutput.parser());
            byte[] bytes = sign.getEncoded().toByteArray();

            return Numeric.toHexString(bytes).substring(2);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private Map<String, TransactionDetailsDTO> collectNodeTxs(TransactionPage page, String address) {
        Map<String, TransactionDetailsDTO> map = new HashMap<>();

        for (int i = 0; i < page.getTx().size(); i++) {
            com.binance.dex.api.client.domain.Transaction t = page.getTx().get(i);

            String txId = t.getTxHash();
            com.belco.server.model.TransactionType type = com.belco.server.model.TransactionType.getType(t.getFromAddr(), t.getToAddr(), address);
            BigDecimal amount = Util.format(new BigDecimal(t.getValue()), 6);
            TransactionStatus status = getStatus(t.getCode());
            long timestamp = ZonedDateTime.parse(t.getTimeStamp()).toInstant().toEpochMilli();

            TransactionDetailsDTO tx = new TransactionDetailsDTO();
            tx.setTxId(txId);
            tx.setType(type.getValue());
            tx.setStatus(status.getValue());
            tx.setCryptoAmount(amount);
            tx.setFromAddress(t.getFromAddr());
            tx.setToAddress(t.getToAddr());
            tx.setTimestamp(timestamp);

            map.put(txId, tx);
        }

        return map;
    }

    private TransactionStatus getStatus(int code) {
        if (code == 0) {
            return TransactionStatus.COMPLETE;
        }

        return TransactionStatus.FAIL;
    }

    private BigDecimal getAmount(String amount) {
        return new BigDecimal(amount).divide(DIVIDER).stripTrailingZeros();
    }
}