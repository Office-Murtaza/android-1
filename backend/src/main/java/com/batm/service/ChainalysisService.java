package com.batm.service;

import com.batm.dto.ChainalysisResponseDTO;
import com.batm.dto.TransactionNumberDTO;
import com.batm.util.Constant;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ChainalysisService {

//    @Autowired
//    private RestTemplate rest;
//
//    @Autowired
//    private TransactionRepository transactionRepository;
//
//    @Value("${chainalysis.mode}")
//    private int chainalysisMode;
//
//    @Value("${chainalysis.url}")
//    private String chainalysisUrl;
//
//    @Value("${chainalysis.api-key}")
//    private String chainalysisApiKey;
//
//    @Value("${chainalysis.rows-limit}")
//    private int chainalysisRowsLimit;
//
//    @Scheduled(fixedDelay = 600_000)
//    public void scheduleChainalysisTransactionRegistrationDelayTask() {
//        if (chainalysisMode == Constant.DISABLED) {
//            return;
//        }
//
//        Set<CoinService.CoinEnum> coins = new HashSet<>(Arrays.asList(CoinService.CoinEnum.BTC, CoinService.CoinEnum.LTC));
//        List<Transaction> untrackedTransactionList = getUntrackedTransactions(coins, chainalysisRowsLimit);
//
//        List<CompletableFuture<ChainalysisResponseDTO>> futures = untrackedTransactionList.stream()
//                .map(this::callAsyncChainalysisValidation)
//                .collect(Collectors.toList());
//
//        List<Transaction> analyzedTransactions = futures.stream()
//                .map(CompletableFuture::join)
//                .filter(Objects::nonNull)
//                .map(ChainalysisResponseDTO::getTransaction)
//                .collect(Collectors.toList());
//
//        saveTransactions(analyzedTransactions);
//    }
//
//    public List<Transaction> getUntrackedTransactions(Set<CoinService.CoinEnum> coins, Integer limit) {
//        Pageable page = PageRequest.of(0, limit);
//        Set<String> currency = coins.stream()
//                .map(Enum::name)
//                .collect(Collectors.toSet());
//
//        return transactionRepository.findUnTrackedClosedTransactions(currency, page);
//    }
//
//    @Transactional
//    public List<Transaction> saveTransactions(List<Transaction> transactions) {
//        return transactionRepository.saveAll(transactions);
//    }
//
//    public CompletableFuture<ChainalysisResponseDTO> callAsyncChainalysisValidation(Transaction transaction) {
//        return CompletableFuture.supplyAsync(() -> {
//            try {
//                transaction.setCryptoAddress(transaction.getCryptoAddress().split(":")[0]);
//
//                CoinService.CoinEnum coinEnum = CoinService.CoinEnum.valueOf(transaction.getCryptoCurrency());
//                TransactionNumberDTO trxNumberDTO = coinEnum.getTransactionNumber(transaction.getCryptoAddress(), transaction.getCryptoAmount());
//                transaction.setDetail(trxNumberDTO.getTransactionId());
//                transaction.setN(trxNumberDTO.getN());
//
//                return validateChainalysisTransfer(transaction);
//            } catch (Exception e) {
//                e.printStackTrace();
//                log.error(transaction.toString());
//            }
//
//            ChainalysisResponseDTO dto = new ChainalysisResponseDTO();
//            transaction.setTracked(true);
//            dto.setTransaction(transaction);
//
//            return dto;
//        });
//    }
//
//    private ChainalysisResponseDTO validateChainalysisTransfer(Transaction transaction) {
//        ChainalysisResponseDTO result = new ChainalysisResponseDTO();
//
//        if (transaction.getDetail() == null) {
//            transaction.setTracked(true);
//            result.setTransaction(transaction);
//            return result;
//        }
//
//        String requestType = transaction.getType() == 0 ? "received" : "sent";
//        String requestTransferReference = transaction.getType() == 0
//                ? String.format("%s:%s", transaction.getDetail(), transaction.getCryptoAddress())
//                : String.format("%s:%d", transaction.getDetail(), transaction.getN());
//
//        JSONObject jsonObject = new JSONObject();
//        jsonObject.put("asset", transaction.getCryptoCurrency());
//        jsonObject.put("transferReference", requestTransferReference);
//
//        JSONArray jsonArray = new JSONArray();
//        jsonArray.add(jsonObject);
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
//        headers.setContentType(MediaType.APPLICATION_JSON);
//        headers.set("Token", chainalysisApiKey);
//
//        HttpEntity<JSONArray> request = new HttpEntity<>(jsonArray, headers);
//
//        String url = chainalysisUrl + "/api/kyt/v1/users/" + "abc" + "/transfers/" + requestType;
//
//        try {
//            ResponseEntity<JSONArray> responseEntity = rest.exchange(url, HttpMethod.POST, request, JSONArray.class);
//            transaction.setTracked(true);
//            result.setTransaction(transaction);
//
//            if (responseEntity.getBody() != null) {
//                JSONObject checkResult = responseEntity.getBody().getJSONObject(0);
//                result.setTransferReference(checkResult.getString("transferReference"));
//                result.setAsset(checkResult.getString("asset"));
//                result.setRating(checkResult.getString("rating"));
//
//                JSONObject cluster = checkResult.getJSONObject("cluster");
//                if (cluster != null) {
//                    result.setClusterName(cluster.getString("name"));
//                    result.setClusterCategory(cluster.getString("category"));
//                }
//            }
//        } catch (HttpClientErrorException he) {
//            System.out.println("-------------------------------------- url:\n");
//            System.out.println(url);
//
//            System.out.println("-------------------------------------- request:\n");
//            System.out.println(request);
//
//            he.printStackTrace();
//            return result;
//        } catch (Exception e) {
//            e.printStackTrace();
//            return result;
//        }
//
//        return result;
//    }
}