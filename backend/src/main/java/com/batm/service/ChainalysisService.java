package com.batm.service;

import com.batm.dto.ChainalysisResponseDTO;
import com.batm.dto.TransactionNumberDTO;
import com.batm.entity.TransactionRecord;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class ChainalysisService {

    @Autowired
    private RestTemplate rest;

    @Value("${chainalysis.enabled}")
    private Boolean enabled;

    @Value("${chainalysis.url}")
    private String url;

    @Value("${chainalysis.api-key}")
    private String apiKey;

    @Value("${chainalysis.rows-limit}")
    private int rowsLimit;

    public List<TransactionRecord> process(List<TransactionRecord> list) {
        try {
            if (enabled) {
                List<CompletableFuture<ChainalysisResponseDTO>> futures = list.stream().map(this::callAsync).collect(Collectors.toList());

                return futures.stream().map(CompletableFuture::join).filter(Objects::nonNull).map(ChainalysisResponseDTO::getTransactionRecord).collect(Collectors.toList());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Collections.EMPTY_LIST;
    }

    private CompletableFuture<ChainalysisResponseDTO> callAsync(TransactionRecord tx) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (tx.getCryptoAddress().contains(":")) {
                    tx.setCryptoAddress(tx.getCryptoAddress().split(":")[0]);
                }

                CoinService.CoinEnum coinEnum = CoinService.CoinEnum.valueOf(tx.getCryptoCurrency());

                if (tx.getTracked() == 0 && Arrays.asList(CoinService.CoinEnum.BTC, CoinService.CoinEnum.LTC).contains(coinEnum)) {
                    if (StringUtils.isEmpty(tx.getDetail()) || (tx.getType() == 1 && tx.getN() == null)) {
                        TransactionNumberDTO numberDTO = coinEnum.getTransactionNumber(tx.getCryptoAddress(), tx.getCryptoAmount(), tx.getTransactionType());

                        if (numberDTO != null) {
                            tx.setDetail(numberDTO.getTxId());
                            tx.setN(numberDTO.getN());
                        }
                    }

                    if (StringUtils.isNotEmpty(tx.getDetail()) && ((tx.getType() == 1 && tx.getN() != null) || tx.getType() == 0)) {
                        sendRequest(tx);
                    } else {
                        tx.setTracked(2);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return new ChainalysisResponseDTO(tx);
        });
    }

    private void sendRequest(TransactionRecord tx) {
        try {
            String requestType = tx.getType() == 0 ? "received" : "sent";
            String requestTransferReference = tx.getType() == 0 ? String.format("%s:%s", tx.getDetail(), tx.getCryptoAddress()) : String.format("%s:%d", tx.getDetail(), tx.getN());

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("asset", tx.getCryptoCurrency());
            jsonObject.put("transferReference", requestTransferReference);

            JSONArray jsonArray = new JSONArray();
            jsonArray.add(jsonObject);

            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Token", apiKey);

            HttpEntity<JSONArray> requestBody = new HttpEntity<>(jsonArray, headers);
            String requestUrl = url + "/api/kyt/v1/users/" + tx.getIdentity().getPublicId() + "/transfers/" + requestType;

            //System.out.println(" ---- txId:" + tx.getId() + "\n" + "url:" + requestUrl + "\n" + "body:" + requestBody + "\n");

            ResponseEntity<JSONArray> res = rest.exchange(requestUrl, HttpMethod.POST, requestBody, JSONArray.class);

            if (res.getStatusCode() == HttpStatus.OK) {
                tx.setTracked(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}