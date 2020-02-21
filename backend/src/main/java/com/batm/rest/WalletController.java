package com.batm.rest;

import com.batm.service.CoinService;
import com.batm.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/wallet")
public class WalletController {

    @Autowired
    private WalletService walletService;

    @GetMapping("/validate")
    public boolean getValidate(@RequestParam CoinService.CoinEnum coinCode, @RequestParam String address) {
        return coinCode.getCoinType().validate(address);
    }

    @GetMapping("/address")
    public String getAddress(@RequestParam CoinService.CoinEnum coinCode) {
        return walletService.getCryptoAddress(coinCode);
    }

    @GetMapping("/price")
    public BigDecimal getPrice(@RequestParam CoinService.CoinEnum coinCode) {
        return coinCode.getPrice();
    }

    @GetMapping("/balance")
    public BigDecimal getBalance(@RequestParam CoinService.CoinEnum coinCode) {
        return walletService.getCryptoBalance(coinCode);
    }

    @GetMapping("/send")
    public String sendCoins(@RequestParam CoinService.CoinEnum coinCode, @RequestParam String toAddress, @RequestParam BigDecimal amount) {
        return walletService.sendCoins(coinCode, toAddress, amount);
    }
}