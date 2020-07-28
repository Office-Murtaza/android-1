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
    private TwilioService twilioService;

    @Autowired
    private UserService userService;

    @Autowired
    private WalletService walletService;

    @Autowired
    private GethService geth;

    @GetMapping("/sms")
    public Response sendSMS(@RequestParam String phone) {
        return Response.ok(twilioService.sendMessage(phone, "This is a test message"));
    }

    @GetMapping("/wallet")
    public Response getWalletAddresses() {
        JSONObject res = new JSONObject();

        res.put("BTC", getCoinJson(walletService.getAddressBTC(), CoinService.CoinEnum.BTC.getBalance(walletService.getAddressBTC()), CoinType.BITCOIN.derivationPath()));
        res.put("BCH", getCoinJson(walletService.getAddressBCH(), CoinService.CoinEnum.BCH.getBalance(walletService.getAddressBCH()), CoinType.BITCOINCASH.derivationPath()));
        res.put("ETH", getCoinJson(walletService.getAddressETH(), CoinService.CoinEnum.ETH.getBalance(walletService.getAddressETH()), CoinType.ETHEREUM.derivationPath()));
        res.put("CATM", getCoinJson(walletService.getAddressETH(), CoinService.CoinEnum.CATM.getBalance(walletService.getAddressETH()), CoinType.ETHEREUM.derivationPath()));
        res.put("LTC", getCoinJson(walletService.getAddressLTC(), CoinService.CoinEnum.LTC.getBalance(walletService.getAddressLTC()), CoinType.LITECOIN.derivationPath()));
        res.put("BNB", getCoinJson(walletService.getAddressBNB(), CoinService.CoinEnum.BNB.getBalance(walletService.getAddressBNB()), CoinType.BINANCE.derivationPath()));
        res.put("XRP", getCoinJson(walletService.getAddressXRP(), CoinService.CoinEnum.XRP.getBalance(walletService.getAddressXRP()), CoinType.XRP.derivationPath()));
        res.put("TRX", getCoinJson(walletService.getAddressTRX(), CoinService.CoinEnum.TRX.getBalance(walletService.getAddressTRX()), CoinType.TRON.derivationPath()));

        return Response.ok(res);
    }

    @GetMapping("/coins/{coinCode}/price")
    public Response price(@PathVariable CoinService.CoinEnum coinCode) {
        return Response.ok(coinCode.getPrice());
    }

    @GetMapping("/coins/{coinCode}/sign")
    public Response sign(@PathVariable CoinService.CoinEnum coinCode, @RequestParam String fromAddress, @RequestParam String toAddress, @RequestParam BigDecimal amount) {
        return Response.ok(coinCode.sign(fromAddress, toAddress, amount));
    }

    @GetMapping("/coins/{coinCode}/submit")
    public Response submit(@PathVariable CoinService.CoinEnum coinCode, @RequestParam String hex) {
        return Response.ok(coinCode.submitTransaction(hex));
    }

    @GetMapping("/coins/eth-nonce")
    public Response ethNonce(@RequestParam String address) {
        return Response.ok(geth.getNonce(address));
    }

    @GetMapping("/user/{userId}/kyc-delete")
    public Response deleteKyc(@PathVariable Long userId) {
        return Response.ok(userService.deleteKyc(userId));
    }

    private JSONObject getCoinJson(String address, BigDecimal balance, String path) {
        JSONObject json = new JSONObject();

        json.put("address", address);
        json.put("balance", balance);
        json.put("path", path);

        return json;
    }
}