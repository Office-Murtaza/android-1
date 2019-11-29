package com.batm.rest;

import com.batm.service.CoinService;
import com.batm.service.MessageService;
import com.batm.service.WalletService;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.batm.model.Response;
import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/test")
public class TestController {

    @Autowired
    private MessageService messageService;

    @Autowired
    private WalletService walletService;

    @GetMapping("/sms")
    public Response sendSMS(@RequestParam String phone) {
        return Response.ok(messageService.sendMessage(phone, "Hey there, this is a test message!!!"));
    }

    @GetMapping("/code/{userId}")
    public Response getCode(@PathVariable Long userId) {
        JSONObject res = new JSONObject();
        res.put("code", messageService.getVerificationCode(userId));

        return Response.ok(res);
    }

    @GetMapping("/wallet")
    public Response getWalletAddresses() {
        JSONObject res = new JSONObject();
        res.put("addressBTC", walletService.getAddressBTC());
        res.put("balanceBTC", CoinService.CoinEnum.BTC.getBalance(walletService.getAddressBTC()));

        res.put("addressBCH", walletService.getAddressBCH());
        res.put("balanceBCH", CoinService.CoinEnum.BCH.getBalance(walletService.getAddressBCH()));

        res.put("addressETH", walletService.getAddressETH());
        res.put("balanceETH", CoinService.CoinEnum.ETH.getBalance(walletService.getAddressETH()));

        res.put("addressLTC", walletService.getAddressLTC());
        res.put("balanceLTC", CoinService.CoinEnum.LTC.getBalance(walletService.getAddressLTC()));

        res.put("addressBNB", walletService.getAddressBNB());
        res.put("balanceBNB", CoinService.CoinEnum.BNB.getBalance(walletService.getAddressBNB()));

        res.put("addressXRP", walletService.getAddressXRP());
        res.put("balanceXRP", CoinService.CoinEnum.XRP.getBalance(walletService.getAddressXRP()));

        res.put("addressTRX", walletService.getAddressTRX());
        res.put("balanceTRX", CoinService.CoinEnum.TRX.getBalance(walletService.getAddressTRX()));

        return Response.ok(res);
    }

    @GetMapping("/wallet/{coin}/sign")
    public Response sign(@PathVariable CoinService.CoinEnum coin, @RequestParam String address, @RequestParam BigDecimal amount) {
        coin.sign(address, amount);

        return Response.ok(true);
    }
}