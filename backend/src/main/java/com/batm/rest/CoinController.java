package com.batm.rest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
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

    @GetMapping("/user/{userId}/coins/{coinId}/transactions")
    public Response getTransactions(@PathVariable Long userId, @PathVariable String coinId, @RequestParam Integer index) {
        JSONParser parser = new JSONParser();
        try {

            JSONObject jsonObject = (JSONObject) parser.parse(" {" +
                    "\"total\": 2," +
                    "\"transactions\": [" +
                    "{" +
                    "\"index\": 1," +
                    "\"txid\": \"b53d6f6614218a6d7a6b23cd89150908e8112d8717dc2ba2c7bf2997a8c16e09\"," +
                    "\"type\": \"withdraw\"," +
                    "\"value\": 0.01," +
                    "\"status\": \"confirmed\"," +
                    "\"date\": \"2019-08-17\"" +
                    "}," +
                    "{" +
                    "\"index\": 2," +
                    "\"txid\": \"5a919ae049ea60249570216b9916dd1381608287fb339f0b3ae068ce949fca29\"," +
                    "\"type\": \"deposit\"," +
                    "\"value\": 0.01," +
                    "\"status\": \"confirmed\"," +
                    "\"date\": \"2019-08-16\"" +
                    "}" +
                    "]" +
                    "}");
            return Response.ok(jsonObject);

        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError();
        }
    }
}