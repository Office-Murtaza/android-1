package com.batm.rest;

import com.batm.dto.SignDTO;
import com.batm.service.CoinService;
import com.batm.service.MessageService;
import com.batm.service.WalletService;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.batm.model.Response;
import wallet.core.jni.CoinType;
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
        return Response.ok(messageService.sendMessage(phone, "Hey there, do you want to buy an elephant?"));
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

        res.put("BTC", getCoinJson(walletService.getAddressBTC(), CoinService.CoinEnum.BTC.getBalance(walletService.getAddressBTC()), CoinType.BITCOIN.derivationPath()));
        res.put("BCH", getCoinJson(walletService.getAddressBCH(), CoinService.CoinEnum.BCH.getBalance(walletService.getAddressBCH()), CoinType.BITCOINCASH.derivationPath()));
        res.put("ETH", getCoinJson(walletService.getAddressETH(), CoinService.CoinEnum.ETH.getBalance(walletService.getAddressETH()), CoinType.ETHEREUM.derivationPath()));
        res.put("LTC", getCoinJson(walletService.getAddressLTC(), CoinService.CoinEnum.LTC.getBalance(walletService.getAddressLTC()), CoinType.LITECOIN.derivationPath()));
        res.put("BNB", getCoinJson(walletService.getAddressBNB(), CoinService.CoinEnum.BNB.getBalance(walletService.getAddressBNB()), CoinType.BINANCE.derivationPath()));
        res.put("XRP", getCoinJson(walletService.getAddressXRP(), CoinService.CoinEnum.XRP.getBalance(walletService.getAddressXRP()), CoinType.XRP.derivationPath()));
        res.put("TRX", getCoinJson(walletService.getAddressTRX(), CoinService.CoinEnum.TRX.getBalance(walletService.getAddressTRX()), CoinType.TRON.derivationPath()));

        return Response.ok(res);
    }

    @GetMapping("/wallet/{coinType}/new")
    public Response getNewWalletAddresses(@PathVariable CoinType coinType) {
        JSONObject res = new JSONObject();

        for (int i = 0; i < 10; i++) {
            String path = walletService.getPath(coinType);
            String newPath = walletService.generateNewPath(path, i);

            res.put(newPath, walletService.generateNewAddress(coinType, newPath));
        }

        return Response.ok(res);
    }

    @GetMapping("/wallet/{coinCode}/sign")
    public Response sign(@PathVariable CoinService.CoinEnum coinCode, @RequestParam String address, @RequestParam BigDecimal amount) {
        SignDTO dto = coinCode.buildSignDTOFromMainWallet();

        return Response.ok(coinCode.sign(address, amount, dto));
    }

    private JSONObject getCoinJson(String address, BigDecimal balance, String path) {
        JSONObject json = new JSONObject();

        json.put("address", address);
        json.put("balance", balance);
        json.put("path", path);

        return json;
    }
}