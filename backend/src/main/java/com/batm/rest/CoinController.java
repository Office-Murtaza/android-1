package com.batm.rest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.batm.model.Error;
import com.batm.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.batm.model.Response;
import com.batm.dto.CoinDTO;
import com.batm.service.CoinService;

@RestController
@RequestMapping("/api/v1")
public class CoinController {

    @Autowired
    private UserService userService;

    @Autowired
    private CoinService coinService;

    @PostMapping("/user/{userId}/coins/add")
    public Response addCoins(@RequestBody CoinDTO coinDTO, @PathVariable Long userId) {
        try {
            if (coinDTO.getCoinList().isEmpty()) {
                return Response.error(new Error(2, "Empty coin list"));
            }

            coinService.save(coinDTO, userId);

            Map<String, Object> response = new HashMap<>();
            response.put("isCoinsAdded", true);

            return Response.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError();
        }
    }

    @PostMapping("/user/{userId}/coins/compare")
    public Response compareCoins(@RequestBody CoinDTO coinDTO, @PathVariable Long userId) {
        try {
            if (coinDTO.getCoinList().isEmpty()) {
                return Response.error(new Error(2, "Empty coin list"));
            }

            return coinService.compareCoins(coinDTO, userId);
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError();
        }
    }

    @GetMapping("/user/{userId}/coins/balance")
    public Response getCoinsBalance(@PathVariable Long userId, @RequestParam List<String> coins) {
        try {
            return Response.ok(coinService.getCoinsBalance(userId, coins));
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError();
        }
    }

    @GetMapping("/user/{userId}/coins/fee")
    public Response getCoinsFee(@PathVariable Long userId, @RequestParam List<String> coins) {
        try {
            return Response.ok(coinService.getCoinsFee(coins));
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError();
        }
    }

    @GetMapping("/user/{userId}/coins/{coinId}/giftaddress")
    public Response getCoinAddressByUserPhone(@PathVariable String userId, @PathVariable CoinService.CoinEnum coinId, @RequestParam String phone) {
        try {
            return Response.ok(userService.getUserGiftAddress(coinId, phone));
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError();
        }
    }
}