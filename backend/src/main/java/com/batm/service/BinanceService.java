package com.batm.service;

import com.batm.dto.CurrentAccountDTO;
import com.batm.dto.SubmitTransactionDTO;
import com.batm.dto.TransactionResponseDTO;
import com.batm.model.TransactionStatus;
import com.batm.util.TransactionUtil;
import com.batm.util.Util;
import com.binance.dex.api.client.BinanceDexApiRestClient;
import com.binance.dex.api.client.domain.Account;
import com.binance.dex.api.client.domain.TransactionMetadata;
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
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Service
public class BinanceService {

    @Autowired
    private BinanceDexApiRestClient binanceDex;

    @Autowired
    private RestTemplate rest;

    @Value("${bnb.url}")
    private String url;

    public BigDecimal getBalance(String address) {
        try {
            return Util.format(binanceDex
                    .getAccount(address)
                    .getBalances()
                    .stream()
                    .filter(e -> "BNB".equals(e.getSymbol()))
                    .map(it -> new BigDecimal(it.getFree()).add(new BigDecimal(it.getLocked())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add), 5);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return BigDecimal.ZERO;
    }

    public String submitTransaction(SubmitTransactionDTO transaction) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.TEXT_PLAIN);

            JSONObject res = JSONArray.fromObject(rest.postForObject(url + "/api/v1/broadcast", transaction.getHex(), String.class)).getJSONObject(0);

            return res.optString("hash");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public TransactionStatus getTransactionStatus(String txId) {
        try {
            TransactionMetadata metadata = binanceDex.getTransactionMetadata(txId);

            if (metadata.isOk()) {
                return TransactionStatus.COMPLETE;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return TransactionStatus.PENDING;
    }

    public TransactionResponseDTO getTransactions(String address, Integer startIndex, Integer limit) {
        TransactionResponseDTO result = new TransactionResponseDTO();

        try {
            TransactionsRequest request = new TransactionsRequest();
            request.setStartTime(new SimpleDateFormat("yyyy").parse("2010").getTime());
            request.setEndTime(new Date().getTime() + TimeUnit.DAYS.toMillis(1));
            request.setTxType(TransactionType.TRANSFER);
            request.setAddress(address);
            request.setTxAsset("BNB");
            request.setLimit(1000);

            TransactionPage page = binanceDex.getTransactions(request);

            return TransactionUtil.composeBinance(page, address, startIndex, limit);
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
}