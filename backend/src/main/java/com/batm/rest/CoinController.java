package com.batm.rest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.batm.model.Error;
import com.batm.util.Constant;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.batm.model.Response;
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
                return Response.error(new Error(2, "Empty coin list"));
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
                return Response.error(new Error(2, "Empty coin list"));
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

    @GetMapping("/user/{userId}/coins/{coinId}/giftaddress")
    public Response getCoinAddressByUserPhone(@PathVariable String userId, @PathVariable CoinService.CoinEnum coinId, @RequestParam String phone) {
        try {
            return Response.ok(coinService.getUserGiftAddress(coinId, phone));
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError();
        }
    }
}