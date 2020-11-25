package com.belco.server.rest;

import java.util.List;

import com.belco.server.model.PricePeriod;
import com.belco.server.model.Response;
import com.belco.server.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class CoinController {

    @Autowired
    private UserService userService;

    @Autowired
    private CoinService coinService;

    @Autowired
    private RippledService rippledService;

    @Autowired
    private CacheService cacheService;

    @GetMapping("/coin/{coin}/details")
    public Response getDetails(@PathVariable CoinService.CoinEnum coin) {
        try {
            return Response.ok(coinService.getCoinDetails(coin));
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError();
        }
    }

    @GetMapping("/coin/{coin}/gift-address")
    public Response getAddressByPhone(@PathVariable CoinService.CoinEnum coin, @RequestParam String phone) {
        try {
            return Response.ok(userService.getCoinAddressByPhone(coin, phone));
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError();
        }
    }

    @GetMapping("/coin/{coin}/utxo")
    public Response getUtxo(@PathVariable CoinService.CoinEnum coin, @RequestParam String xpub) {
        try {
            if (coin == CoinService.CoinEnum.BTC || coin == CoinService.CoinEnum.BCH || coin == CoinService.CoinEnum.LTC) {
                return Response.ok(coin.getUTXO(xpub));
            } else {
                return Response.defaultError(coin.name() + " not allowed");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError();
        }
    }

    @GetMapping("/coin/{coin}/nonce")
    public Response getNonce(@PathVariable CoinService.CoinEnum coin, @RequestParam String address) {
        try {
            if (coin == CoinService.CoinEnum.ETH || coin == CoinService.CoinEnum.CATM) {
                return Response.ok(coin.getNonce(address));
            } else {
                return Response.defaultError(coin.name() + " not allowed");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError();
        }
    }

    @GetMapping("/coin/{coin}/current-account")
    public Response getCurrentAccount(@PathVariable CoinService.CoinEnum coin, @RequestParam String address) {
        try {
            if (coin == CoinService.CoinEnum.BNB || coin == CoinService.CoinEnum.XRP) {
                return Response.ok(coin.getCurrentAccount(address));
            } else {
                return Response.defaultError(coin.name() + " not allowed");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError();
        }
    }

    @GetMapping("/coin/{coin}/current-account-activated")
    public Response getCurrentAccountActivated(@PathVariable CoinService.CoinEnum coin, @RequestParam String address) {
        try {
            if (coin == CoinService.CoinEnum.XRP) {
                return Response.ok(!rippledService.getNodeTransactions(address).getMap().isEmpty());
            } else {
                return Response.defaultError(coin.name() + " not allowed");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError();
        }
    }

    @GetMapping("/coin/{coin}/current-block")
    public Response getCurrentBlock(@PathVariable CoinService.CoinEnum coin) {
        try {
            if (coin == CoinService.CoinEnum.TRX) {
                return Response.ok(coin.getCurrentBlock());
            } else {
                return Response.defaultError(coin.name() + " not allowed");
            }
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