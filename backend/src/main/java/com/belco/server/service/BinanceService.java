package com.belco.server.service;

import com.belco.server.dto.*;
import com.belco.server.model.TransactionStatus;
import com.belco.server.util.TxUtil;
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
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.web3j.utils.Numeric;
import wallet.core.java.AnySigner;
import wallet.core.jni.AnyAddress;
import wallet.core.jni.CoinType;
import wallet.core.jni.PrivateKey;
import wallet.core.jni.proto.Binance;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Getter
@Service
public class BinanceService {

    private static final String CHAIN_ID = "Binance-Chain-Tigris";
    private static final BigDecimal BNB_DIVIDER = BigDecimal.valueOf(100_000_000L);

    @Autowired
    private BinanceDexApiRestClient binanceDex;

    @Autowired
    private MongoOperations mongo;

    @Autowired
    private RestTemplate rest;

    @Autowired
    private WalletService walletService;

    @Value("${bnb.node.url}")
    private String nodeUrl;

    @Value("${bnb.explorer.url}")
    private String explorerUrl;

    private boolean isNodeAvailable;

    @PostConstruct
    public void init() {
        if (StringUtils.isNotBlank(nodeUrl)) {
            isNodeAvailable = true;
        }
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

    public String submitTransaction(String hex) {
        if (isNodeAvailable) {
            try {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.TEXT_PLAIN);

                JSONObject res = JSONArray.fromObject(rest.postForObject(nodeUrl + "/api/v1/broadcast", hex, String.class)).getJSONObject(0);

                return res.optString("hash");
            } catch (ResourceAccessException rae) {
                isNodeAvailable = false;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    public TransactionDetailsDTO getTransaction(String txId, String address) {
        TransactionDetailsDTO dto = new TransactionDetailsDTO();

        if (isNodeAvailable) {
            try {
                JSONObject res = rest.getForObject(nodeUrl + "/api/v1/tx/" + txId + "?format=json", JSONObject.class);
                JSONObject msg = res.optJSONObject("tx").optJSONObject("value").optJSONArray("msg").getJSONObject(0);

                dto.setTxId(txId);
                dto.setLink(explorerUrl + "/" + txId);
                dto.setFromAddress(msg.optJSONObject("value").optJSONArray("inputs").getJSONObject(0).optString("address"));
                dto.setToAddress(msg.optJSONObject("value").optJSONArray("outputs").getJSONObject(0).optString("address"));
                dto.setType(com.belco.server.model.TransactionType.getType(dto.getFromAddress(), dto.getToAddress(), address));
                dto.setStatus(getStatus(res.getInt("code")));
                dto.setCryptoAmount(getAmount(msg.optJSONObject("value").optJSONArray("inputs").getJSONObject(0).getJSONArray("coins").getJSONObject(0).optString("amount")));
                dto.setCryptoFee(getAmount("1000000"));
            } catch (ResourceAccessException rae) {
                isNodeAvailable = false;
                dto.setStatus(TransactionStatus.PENDING);
            } catch (Exception e) {
                e.printStackTrace();
                dto.setStatus(TransactionStatus.FAIL);
            }
        }

        return dto;
    }

    public NodeTransactionsDTO getNodeTransactions(String address) {
        try {
            TransactionsRequest request = new TransactionsRequest();
            request.setStartTime(new SimpleDateFormat("yyyy").parse("2010").getTime());
            request.setEndTime(new Date().getTime() + TimeUnit.DAYS.toMillis(1));
            request.setTxType(TransactionType.TRANSFER);
            request.setAddress(address);
            request.setTxAsset("BNB");
            request.setLimit(1000);

            TransactionPage page = binanceDex.getTransactions(request);

            return new NodeTransactionsDTO(collectNodeTxs(page, address));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new NodeTransactionsDTO();
    }

    public TransactionListDTO getTransactionList(String address, Integer startIndex, Integer limit, TxListDTO txDTO) {
        try {
            Map<String, TransactionDetailsDTO> map = getNodeTransactions(address).getMap();

            return TxUtil.buildTxs(map, startIndex, limit, txDTO);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new TransactionListDTO();
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

    public String sign(String fromAddress, String toAddress, BigDecimal amount) {
        try {
            PrivateKey privateKey;

            if (walletService.isServerAddress(CoinType.BINANCE, fromAddress)) {
                privateKey = walletService.getCoinsMap().get(CoinType.BINANCE).getPrivateKey();
            } else {
                String path = walletService.getPath(fromAddress);
                privateKey = walletService.getWallet().getKey(CoinType.BINANCE, path);
            }

            CurrentAccountDTO currentDTO = getCurrentAccount(fromAddress);

            Binance.SigningInput.Builder builder = Binance.SigningInput.newBuilder();
            builder.setChainId(currentDTO.getChainId());
            builder.setAccountNumber(currentDTO.getAccountNumber());
            builder.setSequence(currentDTO.getSequence());
            builder.setPrivateKey(ByteString.copyFrom(privateKey.data()));

            Binance.SendOrder.Token.Builder token = Binance.SendOrder.Token.newBuilder();
            token.setDenom("BNB");
            token.setAmount(amount.multiply(BNB_DIVIDER).longValue());

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
            com.binance.dex.api.client.domain.Transaction tx = page.getTx().get(i);

            String txId = tx.getTxHash();
            com.belco.server.model.TransactionType type = com.belco.server.model.TransactionType.getType(tx.getFromAddr(), tx.getToAddr(), address);
            BigDecimal amount = Util.format(new BigDecimal(tx.getValue()), 6);
            TransactionStatus status = getStatus(tx.getCode());
            Date date1 = Date.from(ZonedDateTime.parse(tx.getTimeStamp()).toInstant());

            map.put(txId, new TransactionDetailsDTO(txId, amount, tx.getFromAddr(), tx.getToAddr(), type, status, date1));
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
        return new BigDecimal(amount).divide(BNB_DIVIDER).stripTrailingZeros();
    }
}