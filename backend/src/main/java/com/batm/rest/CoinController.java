package com.batm.rest;

import java.util.List;
import com.batm.model.Error;
import com.batm.service.SolrService;
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
    @Autowired
    private SolrService solrService;

    @PostMapping("/user/{userId}/coins/add")
    public Response addCoins(@RequestBody CoinDTO coinDTO, @PathVariable Long userId) {
        try {
            if (coinDTO.getCoins().isEmpty()) {
                return Response.error(new Error(2, "Empty coin list"));
            }

            coinService.save(coinDTO, userId);

            return Response.ok(true);
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError();
        }
    }

    @PostMapping("/user/{userId}/coins/compare")
    public Response compareCoins(@RequestBody CoinDTO coinDTO, @PathVariable Long userId) {
        try {
            if (coinDTO.getCoins().isEmpty()) {
                return Response.error(new Error(2, "Empty coin list"));
            }

            if (coinService.compareCoins(coinDTO, userId)) {
                return Response.ok(true);
            } else {
                return Response.error(new Error(3, "Coins do not match"));
            }
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
    public Response getCoinsFee(@PathVariable Long userId) {
        try {
            return Response.ok(coinService.getCoinsFee());
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError();
        }
    }

    @GetMapping("/user/{userId}/coins/{coinCode}/giftaddress")
    public Response getCoinAddressByUserPhone(@PathVariable String userId, @PathVariable CoinService.CoinEnum coinCode, @RequestParam String phone) {
        try {
            return Response.ok(userService.getUserGiftAddress(coinCode, phone));
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError();
        }
    }

    @GetMapping("/coins/{coinCode}/price-chart")
    public Response getPriceChart(@PathVariable CoinService.CoinEnum coinCode) {
        try {
            return Response.ok(solrService.collectPriceChartData(coinCode));
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError();
        }
    }

    @DeleteMapping("/coins/price-chart")
    public Response getPriceChart() {
        try {
            solrService.cleanAllCoinPrice();
            return Response.ok("OK");
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError();
        }
    }
}