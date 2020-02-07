package com.batm.rest;

import com.batm.dto.WalletDTO;
import com.batm.model.Response;
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

    @GetMapping("/address")
    public Response getCryptoAddress(@RequestParam CoinService.CoinEnum cryptoCurrency) {
        try {
            WalletDTO dto = new WalletDTO();
            dto.setAddress(walletService.getCryptoAddress(cryptoCurrency));

            return Response.ok(dto);
        } catch (Exception e) {
            e.printStackTrace();
            return Response.ok(new WalletDTO());
        }
    }

    @GetMapping("/balance")
    public Response getCryptoBalance(@RequestParam CoinService.CoinEnum cryptoCurrency) {
        try {
            WalletDTO dto = new WalletDTO();
            dto.setBalance(walletService.getCryptoBalance(cryptoCurrency));

            return Response.ok(dto);
        } catch (Exception e) {
            e.printStackTrace();
            return Response.ok(new WalletDTO());
        }
    }

    @GetMapping("/send")
    public Response sendCoins(@RequestParam String destinationAddress, @RequestParam BigDecimal amount, @RequestParam CoinService.CoinEnum cryptoCurrency, @RequestParam String description) {
        try {
            WalletDTO dto = new WalletDTO();
            dto.setTxId(walletService.sendCoins(destinationAddress, amount, cryptoCurrency, description));

            return Response.ok(dto);
        } catch (Exception e) {
            e.printStackTrace();
            return Response.ok(new WalletDTO());
        }
    }
}