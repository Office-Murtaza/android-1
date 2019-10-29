package com.batm.service;

import com.batm.dto.ChainalysisResponseDTO;
import com.batm.entity.TransactionRecord;
import com.batm.repository.TransactionRecordRepository;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class ChainalysisService {

    @Autowired
    private RestTemplate rest;

    @Autowired
    private TransactionRecordRepository transactionRecordRepository;

    @Value("${chainalysis.enabled}")
    private Boolean enabled;

    @Value("${chainalysis.url}")
    private String url;

    @Value("${chainalysis.api-key}")
    private String apiKey;

    @Value("${chainalysis.rows-limit}")
    private int rowsLimit;

    @Scheduled(fixedDelay = 900_000)
    public void processUntrackedTransactions() {
        try {
            if (enabled) {
                List<TransactionRecord> untrackedList = transactionRecordRepository.findUntrackedAndClosedTransactions(Arrays.asList(CoinService.CoinEnum.BTC.name(), CoinService.CoinEnum.LTC.name()), PageRequest.of(0, rowsLimit));

                List<CompletableFuture<ChainalysisResponseDTO>> futures = untrackedList
                        .stream().map(this::callAsync).collect(Collectors.toList());

                List<TransactionRecord> trackedList = futures.stream()
                        .map(CompletableFuture::join)
                        .filter(Objects::nonNull)
                        .map(ChainalysisResponseDTO::getTransactionRecord)
                        .collect(Collectors.toList());

                transactionRecordRepository.saveAll(trackedList);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private CompletableFuture<ChainalysisResponseDTO> callAsync(TransactionRecord tx) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (tx.getCryptoAddress().contains(":")) {
                    tx.setCryptoAddress(tx.getCryptoAddress().split(":")[0]);
                }

                CoinService.CoinEnum coinEnum = CoinService.CoinEnum.valueOf(tx.getCryptoCurrency());

                sendRequest(tx, coinEnum);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return new ChainalysisResponseDTO(tx);
        });
    }

    private void sendRequest(TransactionRecord tx, CoinService.CoinEnum coinId) {
        try {
            String requestType = tx.getType() == 0 ? "received" : "sent";
            String requestTransferReference = tx.getType() == 0
                    ? String.format("%s:%s", tx.getDetail(), tx.getCryptoAddress())
                    : String.format("%s:%d", tx.getDetail(), coinId.getN(tx.getDetail(), tx.getCryptoAddress()));

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("asset", tx.getCryptoCurrency());
            jsonObject.put("transferReference", requestTransferReference);

            JSONArray jsonArray = new JSONArray();
            jsonArray.add(jsonObject);

            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Token", apiKey);

            HttpEntity<JSONArray> request = new HttpEntity<>(jsonArray, headers);
            String requestUrl = url + "/api/kyt/v1/users/" + tx.getIdentity().getPublicId() + "/transfers/" + requestType;
            ResponseEntity<JSONArray> res = rest.exchange(requestUrl, HttpMethod.POST, request, JSONArray.class);

            if (res.getStatusCode() == HttpStatus.OK) {
                tx.setTracked(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}