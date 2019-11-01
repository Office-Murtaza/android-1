package com.batm.service;

import com.batm.dto.CurrentAccountDTO;
import com.batm.dto.SubmitTransactionDTO;
import com.batm.dto.TransactionDTO;
import com.batm.dto.TransactionListDTO;
import com.batm.entity.TransactionRecord;
import com.batm.entity.TransactionRecordGift;
import com.batm.model.TransactionStatus;
import com.batm.util.Constant;
import com.batm.util.Util;
import com.binance.dex.api.client.BinanceDexApiRestClient;
import com.binance.dex.api.client.domain.Account;
import com.binance.dex.api.client.domain.TransactionPage;
import com.binance.dex.api.client.domain.TransactionType;
import com.binance.dex.api.client.domain.request.TransactionsRequest;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class BinanceService {

    @Autowired
    private BinanceDexApiRestClient binanceDex;

    @Autowired
    private RestTemplate rest;

    @Value("${bnb.node.url}")
    private String nodeUrl;

    @Value("${bnb.explorer.url}")
    private String explorerUrl;

    public BigDecimal getBalance(String address) {
        try {
            return Util.format5(binanceDex
                    .getAccount(address)
                    .getBalances()
                    .stream()
                    .filter(e -> "BNB".equals(e.getSymbol()))
                    .map(it -> new BigDecimal(it.getFree()).add(new BigDecimal(it.getLocked())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return BigDecimal.ZERO;
    }

    public String submitTransaction(SubmitTransactionDTO transaction) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.TEXT_PLAIN);

            JSONObject res = JSONArray.fromObject(rest.postForObject(nodeUrl + "/api/v1/broadcast", transaction.getHex(), String.class)).getJSONObject(0);

            return res.optString("hash");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public TransactionStatus getTransactionStatus(String txId) {
        try {
            JSONObject res = rest.getForObject(nodeUrl + "/api/v1/tx/" + txId + "?format=json", JSONObject.class);

            return getStatus(res.getInt("code"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return TransactionStatus.PENDING;
    }

    public TransactionDTO getTransaction(String txId, String address) {
        TransactionDTO dto = new TransactionDTO();

        try {
            JSONObject res = rest.getForObject(nodeUrl + "/api/v1/tx/" + txId + "?format=json", JSONObject.class);
            JSONObject msg = res.optJSONObject("tx").optJSONObject("value").optJSONArray("msg").getJSONObject(0);

            dto.setTxId(txId);
            dto.setLink(explorerUrl + "/" + txId);
            dto.setFromAddress(msg.optJSONObject("value").optJSONArray("inputs").getJSONObject(0).optString("address"));
            dto.setToAddress(msg.optJSONObject("value").optJSONArray("outputs").getJSONObject(0).optString("address"));
            dto.setType(com.batm.model.TransactionType.getType(dto.getFromAddress(), dto.getToAddress(), address));
            dto.setStatus(getStatus(res.getInt("code")));
            dto.setCryptoAmount(getAmount(msg.optJSONObject("value").optJSONArray("inputs").getJSONObject(0).getJSONArray("coins").getJSONObject(0).optString("amount")));
            dto.setCryptoFee(getAmount("1000000"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return dto;
    }

    public TransactionListDTO getTransactionList(String address, Integer startIndex, Integer limit, List<TransactionRecordGift> gifts, List<TransactionRecord> txs) {
        TransactionListDTO result = new TransactionListDTO();

        try {
            TransactionsRequest request = new TransactionsRequest();
            request.setStartTime(new SimpleDateFormat("yyyy").parse("2010").getTime());
            request.setEndTime(new Date().getTime() + TimeUnit.DAYS.toMillis(1));
            request.setTxType(TransactionType.TRANSFER);
            request.setAddress(address);
            request.setTxAsset("BNB");
            request.setLimit(1000);

            TransactionPage page = binanceDex.getTransactions(request);
            Map<String, TransactionDTO> map = collectNodeTxs(page, address);

            return Util.buildTxs(map, startIndex, limit, gifts, txs);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    public CurrentAccountDTO getCurrentAccount(String address) {
        try {
            Account account = binanceDex.getAccount(address);

            return new CurrentAccountDTO(account.getAccountNumber(), account.getSequence());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new CurrentAccountDTO();
    }

    private Map<String, TransactionDTO> collectNodeTxs(TransactionPage page, String address) {
        Map<String, TransactionDTO> map = new HashMap<>();

        for (int i = 0; i < page.getTx().size(); i++) {
            com.binance.dex.api.client.domain.Transaction tx = page.getTx().get(i);

            String txId = tx.getTxHash();
            com.batm.model.TransactionType type = com.batm.model.TransactionType.getType(tx.getFromAddr(), tx.getToAddr(), address);
            BigDecimal amount = Util.format5(new BigDecimal(tx.getValue()));
            TransactionStatus status = getStatus(tx.getCode());
            Date date1 = Date.from(ZonedDateTime.parse(tx.getTimeStamp()).toInstant());

            map.put(txId, new TransactionDTO(txId, amount, type, status, date1));
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
        return new BigDecimal(amount).divide(Constant.BNB_DIVIDER).stripTrailingZeros();
    }
}