package com.batm.rest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.batm.dto.TransactionDTO;
import com.batm.dto.TransactionResponseDTO;
import com.batm.entity.Error;
import com.batm.rest.vm.TxVM;
import com.batm.util.Constant;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.batm.entity.Response;
import com.batm.rest.vm.CoinVM;
import com.batm.service.CoinService;

@RestController
@RequestMapping("/api/v1")
public class CoinController {

    @Autowired
    private CoinService coinService;

    @PostMapping("/user/{userId}/coins/add")
    public Response addCoins(@RequestBody CoinVM coinVM, @PathVariable Long userId) {
        try {
            if (coinVM == null || coinVM.getCoins().isEmpty()) {
                return Response.error(new com.batm.entity.Error(2, "Empty coin list"));
            }

            coinService.save(coinVM, userId);

            Map<String, Object> response = new HashMap<>();
            response.put("isCoinsAdded", true);

            return Response.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError();
        }
    }

    @PostMapping("/user/{userId}/coins/compare")
    public Response compareCoins(@RequestBody CoinVM coinVM, @PathVariable Long userId) {
        try {
            if (coinVM == null || coinVM.getCoins().isEmpty()) {
                return Response.error(new com.batm.entity.Error(2, "Empty coin list"));
            }

            return coinService.compareCoins(coinVM, userId);
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError();
        }
    }

    @GetMapping("/user/{userId}/coins/balance")
    public Response getCoinsBalance(@PathVariable Long userId, @RequestParam(required = false) List<String> coins) {
        try {
            return Response.ok(coinService.getCoinsBalance(userId, coins));
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError();
        }
    }

    @GetMapping("/user/{userId}/coins/{coinId}/publicKey")
    public Response getCoinAddressByUserPhone(@PathVariable String userId, @PathVariable CoinService.CoinEnum coinId, @RequestParam String phone) {
        try {
            Pattern pattern = Pattern.compile(Constant.REGEX_PHONE);
            Matcher matcher = pattern.matcher(phone.replace("\"", ""));
            if (!matcher.find()) {
                return Response.error(new Error(2, "Invalid phone number"));
            }

            String address = coinService.getCoinAddressByUserPhoneAndCoin(matcher.group(0), coinId.name());
            if (address == null) {
                address = coinService.getDefaultPublicKeyByCoin(coinId);
            }

            JSONObject jsonResponse = new JSONObject();
            jsonResponse.put("publicKey", address);
            return Response.ok(jsonResponse);
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError();
        }
    }

    @GetMapping("/user/{userId}/coins/{coinId}/transactions")
    public Response getTransactions(@PathVariable Long userId, @PathVariable CoinService.CoinEnum coinId, @RequestParam(required = false) Integer index) {
        index = index == null || index <= 0 ? 1 : index;
        try {
            TransactionResponseDTO<TransactionDTO> transactionResponseDTO = coinService.getTransactions(userId, coinId, index);
            List<TransactionDTO> transactions = transactionResponseDTO.getTransactions();

            int limit = Math.min(transactions.size(), (10 + index));

            JSONObject response = new JSONObject();
            response.put("total", transactionResponseDTO.getTxs());

            if (transactions != null && transactions.size() >= limit && index <= limit) {
                response.put("transactions", transactions.subList(index - 1, limit - 1));
            } else {
                response.put("transactions", new ArrayList<>());
            }

            return Response.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError();
        }
    }

    @PostMapping("/user/{userId}/coins/{coinId}/transaction")
    public Response submitTransaction(@PathVariable Long userId, @PathVariable CoinService.CoinEnum coinId, @RequestBody TxVM txVM) {
        try {
            JSONObject response = new JSONObject();
            response.put("tx", coinId.submitTransaction(txVM.getHex()));

            return Response.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError();
        }
    }
}