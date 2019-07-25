package com.batm.rest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.batm.dto.UserCoinDTO;
import com.batm.entity.Response;
import com.batm.entity.UserCoin;
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

            Map<String, String> response = new HashMap<>();
            response.put("userId", userId + "");
            response.put("isCoinsAdded", true + "");
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

            Response errorResponse = coinService.compareCoins(coinVM, userId);
            if(errorResponse != null) {
            	return errorResponse;
            }

            Map<String, String> response = new HashMap<>();
            response.put("isCoinsMatched", true + "");

            return Response.ok(response);
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
}