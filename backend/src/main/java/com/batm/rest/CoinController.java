package com.batm.rest;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.batm.dto.CoinBalanceDTO;
import com.batm.dto.Price;
import com.batm.dto.UserCoinDTO;
import com.batm.entity.Response;
import com.batm.entity.UserCoin;
import com.batm.rest.vm.CoinBalanceVM;
import com.batm.rest.vm.CoinVM;
import com.batm.service.CoinService;
import com.binance.api.client.BinanceApiRestClient;

@RestController
@RequestMapping("/api/v1")
public class CoinController {

    @Autowired
    private CoinService coinService;

    @Autowired
    private BinanceApiRestClient binanceApiRestClient;

    @PostMapping("/user/{userId}/coins/add")
    public Response addCoins(@RequestBody CoinVM coinVM, @PathVariable Long userId) {
        if (coinVM == null || coinVM.getCoins().isEmpty()) {
            return Response.error(new com.batm.entity.Error(1, "Empty coin list."));
        }
        try {
            coinService.save(coinVM, userId);
        } catch (Exception e) {
            return Response.error(new com.batm.entity.Error(1, "Something has been wrong."));
        }
        Map<String, String> response = new HashMap<>();
        response.put("userId", userId + "");
        response.put("isCoinsAdded", true + "");
        return Response.ok(response);

    }

    @PostMapping("/user/{userId}/coins/compare")
    public Response compareCoins(@RequestBody CoinVM coinVM, @PathVariable Long userId) {
        if (coinVM == null || coinVM.getCoins().isEmpty()) {
            return Response.error(new com.batm.entity.Error(1, "Empty coin list."));
        }
        try {
            for (UserCoinDTO userCoin : coinVM.getCoins()) {
                UserCoin coinWithUserIdAndCoinCode = this.coinService.getCoinWithUserIdAndCoinCode(userId,
                        userCoin.getCoinCode());
                if (userCoin.getPublicKey() == null
                        || !userCoin.getPublicKey().equalsIgnoreCase(coinWithUserIdAndCoinCode.getPublicKey())) {
                    return Response.error(new com.batm.entity.Error(1, "Public keys not match."));
                }
            }

        } catch (Exception e) {
            return Response.error(new com.batm.entity.Error(1, "Something has been wrong."));
        }

        Map<String, String> response = new HashMap<>();
        response.put("isCoinsMatched", true + "");

        return Response.ok(response);

    }

    @GetMapping("/user/{userId}/coins/balance")
    public Response getCoinsBalance(@PathVariable Long userId, @RequestParam List<String> coins) {
        if(coins.isEmpty()) {
            return Response.error(new com.batm.entity.Error(2, "Empty coins"));
        }



//        List<CoinBalanceDTO> balances = new ArrayList<>();
        try {
            return Response.ok(coinService.getCoinsBalance(userId, coins));
//            BigDecimal totalBalance = new BigDecimal("0").setScale(2, RoundingMode.DOWN);
//            List<UserCoin> userCoins = this.coinService.getCoinByUserId(userId);
//            for (UserCoin userCoin : userCoins) {
//                String coinCode = userCoin.getCoin().getId();
//                if (coinCode.equalsIgnoreCase("BCH")) {
//                    coinCode = "BCHABC";
//                }
//
//                String prc = binanceApiRestClient.getPrice(coinCode + "USDT").getPrice();
//                BigDecimal price = new BigDecimal(prc).setScale(2, RoundingMode.DOWN);
//                totalBalance = totalBalance.add(price);
//                balances.add(new CoinBalanceDTO(userCoin.getCoin().getId(), userCoin.getPublicKey(),
//                        new BigDecimal("1"), new Price(price), userCoin.getCoin().getOrderIndex()));
//            }
//
//            Comparator<CoinBalanceDTO> sortingByIndex = Comparator.comparing(CoinBalanceDTO::getOrderIndex);
//
//            balances.sort(sortingByIndex);
//            //return Response.ok(new CoinBalanceVM(userId, balances, new Price(totalBalance)));
        } catch (Exception e) {
            return Response.error(new com.batm.entity.Error(1, "Something has been wrong."));
        }
    }
}