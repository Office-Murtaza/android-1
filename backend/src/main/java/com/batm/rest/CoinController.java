package com.batm.rest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.json.simple.JSONArray;
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

    //TODO: update with real data
    @GetMapping("/user/{userId}/coins/{coinId}/transactions")
    public Response getTransactions(@PathVariable Long userId, @PathVariable Long coinId, @RequestParam Integer index) {
        try {
            if(index > 2 || index < 1) throw new Exception();

            JSONArray array = new JSONArray();
            String[] statuses = new String[] {"pending", "confirmed", "fail", "unknown"};
            String[] types = new String[] {"withdraw", "deposit"};
            int count = index == 1 ? 10 : 2;

            for (int i = 1; i <=count; i++) {
                JSONObject o = new JSONObject();
                o.put("index", count > 2 ? i : i + 10);
                o.put("txid", "b53d6f6614218a6d7a6b23cd89150908e8112d8717dc2ba2c7bf2997a8c16e09");
                o.put("type", types[new Random().ints(1, 0, 2).sum()]);
                o.put("value", 0.01);
                o.put("status", statuses[new Random().ints(1, 0, 4).sum()]);
                o.put("date", "2019-08-17");

                array.add(o);
            }

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("total", 12);
            jsonObject.put("transactions", array);

            return Response.ok(jsonObject);

        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError();
        }
    }
}