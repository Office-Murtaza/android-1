package com.batm.rest;

import java.util.List;
import com.batm.dto.CoinSettingsDTO;
import com.batm.service.PriceChartService;
import com.batm.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.batm.model.Response;
import com.batm.service.CoinService;

@RestController
@RequestMapping("/api/v1")
public class CoinController {

    @Autowired
    private UserService userService;

    @Autowired
    private CoinService coinService;

    @Autowired
    private PriceChartService chartService;

    @GetMapping("/coins/{coinCode}/settings")
    public Response getCoinsSettings(@PathVariable CoinService.CoinEnum coinCode) {
        try {
            CoinSettingsDTO dto = coinCode.getCoinSettings();
            dto.setCode(coinCode.name());

            return Response.ok(dto);
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError();
        }
    }

    @GetMapping("/coins/{coinCode}/gift-address")
    public Response getCoinAddressByPhone(@PathVariable CoinService.CoinEnum coinCode, @RequestParam String phone) {
        try {
            return Response.ok(userService.getCoinAddressByPhone(coinCode, phone));
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError();
        }
    }

    @GetMapping("/coins/{coinCode}/utxo")
    public Response getUtxo(@PathVariable CoinService.CoinEnum coinCode, @RequestParam String xpub) {
        try {
            if (coinCode == CoinService.CoinEnum.BTC || coinCode == CoinService.CoinEnum.BCH || coinCode == CoinService.CoinEnum.LTC) {
                return Response.ok(coinCode.getUTXO(xpub));
            } else {
                return Response.defaultError(coinCode.name() + " not allowed");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError();
        }
    }

    @GetMapping("/coins/{coinCode}/nonce")
    public Response getNonce(@PathVariable CoinService.CoinEnum coinCode, @RequestParam String address) {
        try {
            if (coinCode == CoinService.CoinEnum.ETH || coinCode == CoinService.CoinEnum.CATM) {
                return Response.ok(coinCode.getNonce(address));
            } else {
                return Response.defaultError(coinCode.name() + " not allowed");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError();
        }
    }

    @GetMapping("/coins/{coinCode}/current-account")
    public Response getCurrentAccount(@PathVariable CoinService.CoinEnum coinCode, @RequestParam String address) {
        try {
            if (coinCode == CoinService.CoinEnum.BNB || coinCode == CoinService.CoinEnum.XRP) {
                return Response.ok(coinCode.getCurrentAccount(address));
            } else {
                return Response.defaultError(coinCode.name() + " not allowed");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError();
        }
    }

    @GetMapping("/coins/{coinCode}/current-block")
    public Response getCurrentBlock(@PathVariable CoinService.CoinEnum coinCode) {
        try {
            if (coinCode == CoinService.CoinEnum.TRX) {
                return Response.ok(coinCode.getCurrentBlock());
            } else {
                return Response.defaultError(coinCode.name() + " not allowed");
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

    @GetMapping("/user/{userId}/coins/{coinCode}/price-chart")
    public Response getPriceChart(@PathVariable Long userId, @PathVariable CoinService.CoinEnum coinCode) {
        try {
            return Response.ok(chartService.getPriceChart(userId, coinCode));
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError();
        }
    }
}