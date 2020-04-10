package com.batm.rest;

import com.batm.service.*;
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
    private UserService userService;

    @Autowired
    private WalletService walletService;

    @Autowired
    private SolrService solrService;

    @Autowired
    private BinanceService binance;

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

    @GetMapping("/price/{coinCode}")
    public Response getPrice(@PathVariable CoinService.CoinEnum coinCode) {
        return Response.ok(coinCode.getPrice());
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
    public Response sign(@PathVariable CoinService.CoinEnum coinCode, @RequestParam String toAddress, @RequestParam BigDecimal amount) {
        return Response.ok(coinCode.sign(coinCode.getWalletAddress(), toAddress, amount));
    }

    private JSONObject getCoinJson(String address, BigDecimal balance, String path) {
        JSONObject json = new JSONObject();

        json.put("address", address);
        json.put("balance", balance);
        json.put("path", path);

        return json;
    }

    @GetMapping("/coins/price-chart")
    public Response submitPriceChart() {
        try {
            binance.persistPrice();

            return Response.ok(true);
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError();
        }
    }

    @DeleteMapping("/coins/price-chart")
    public Response getPriceChart() {
        try {
            solrService.cleanAllCoinPrice();

            return Response.ok(true);
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError();
        }
    }

    @DeleteMapping("/user/{userId}/kyc")
    public Response sign(@PathVariable Long userId) {
        return Response.ok(userService.resetVerificationsForUser(userId));
    }
}