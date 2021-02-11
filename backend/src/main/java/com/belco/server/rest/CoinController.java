package com.belco.server.rest;

import com.belco.server.model.PricePeriod;
import com.belco.server.model.Response;
import com.belco.server.service.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class CoinController {

    private final UserService userService;
    private final CoinService coinService;
    private final BlockbookService blockbookService;
    private final RippledService rippledService;
    private final TrongridService trongridService;
    private final GethService gethService;
    private final CacheService cacheService;

    public CoinController(UserService userService, CoinService coinService, BlockbookService blockbookService, RippledService rippledService, TrongridService trongridService, GethService gethService, CacheService cacheService) {
        this.userService = userService;
        this.coinService = coinService;
        this.blockbookService = blockbookService;
        this.rippledService = rippledService;
        this.trongridService = trongridService;
        this.gethService = gethService;
        this.cacheService = cacheService;
    }

    @GetMapping("/coin/{coin}/details")
    public Response getDetails(@PathVariable CoinService.CoinEnum coin) {
        try {
            return Response.ok(coinService.getCoinDetails(coin));
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError();
        }
    }

    @GetMapping("/coin/{coin}/transfer-address")
    public Response getTransferAddress(@PathVariable CoinService.CoinEnum coin, @RequestParam String phone) {
        try {
            return Response.ok("address", userService.getTransferAddress(coin, phone));
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError();
        }
    }

    @GetMapping("/coin/{coin}/utxo")
    public Response getUtxo(@PathVariable CoinService.CoinEnum coin, @RequestParam String xpub) {
        try {
            return Response.ok("utxos", blockbookService.getUtxo(coin.getCoinType(), xpub));
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError();
        }
    }

    @GetMapping("/coin/{coin}/nonce")
    public Response getNonce(@PathVariable CoinService.CoinEnum coin, @RequestParam String address) {
        try {
            return Response.ok("nonce", gethService.getNonce(address));
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError();
        }
    }

    @GetMapping("/coin/{coin}/current-account")
    public Response getCurrentAccount(@PathVariable CoinService.CoinEnum coin, @RequestParam String address) {
        try {
            return Response.ok(coin.getCurrentAccount(address));
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError();
        }
    }

    @GetMapping("/coin/{coin}/current-account-activated")
    public Response getCurrentAccountActivated(@PathVariable CoinService.CoinEnum coin, @RequestParam String address) {
        try {
            return Response.ok(!rippledService.getNodeTransactions(address).isEmpty());
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError();
        }
    }

    @GetMapping("/coin/{coin}/current-block")
    public Response getCurrentBlock(@PathVariable CoinService.CoinEnum coin) {
        try {
            return Response.ok(trongridService.getCurrentBlock());
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError();
        }
    }

    @GetMapping("/coin/{coin}/price-chart")
    public Response getPriceChart(@PathVariable CoinService.CoinEnum coin, @RequestParam int period) {
        try {
            return Response.ok("prices", cacheService.getPriceChartById(coin.getName(), PricePeriod.valueOf(period)));
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError();
        }
    }

    @GetMapping("/user/{userId}/balance")
    public Response getBalance(@PathVariable Long userId, @RequestParam(required = false) List<String> coins) {
        try {
            return Response.ok(coinService.getCoinsBalance(userId, coins));
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError();
        }
    }

    @GetMapping("/user/{userId}/coin/{coin}/manage")
    public Response enableCoin(@PathVariable Long userId, @PathVariable CoinService.CoinEnum coin, @RequestParam boolean enabled) {
        try {
            return Response.ok(coinService.enableCoin(userId, coin, enabled));
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError();
        }
    }
}