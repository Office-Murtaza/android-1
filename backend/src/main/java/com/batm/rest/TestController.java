package com.batm.rest;

import com.batm.dto.SubmitTransactionDTO;
import com.batm.service.*;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.batm.model.Response;
import wallet.core.jni.CoinType;

import java.math.BigDecimal;
import java.math.BigInteger;

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
    private GethService gethService;

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

    @GetMapping("/stake")
    public Response getStake() {
        JSONObject res = new JSONObject();

        try {
            res.put("totalStakes", new BigDecimal(gethService.getToken().totalStakes().send()).divide(gethService.ETH_DIVIDER));
            res.put("basePeriod(s)", gethService.getToken().basePeriod().send());
            res.put("holdPeriod(s)", gethService.getToken().holdPeriod().send());
            res.put("annualPercent", gethService.getToken().annualPercent().send());
            res.put("annualPeriod(s)", gethService.getToken().annualPeriod().send());

            gethService.getToken().stakeParams(BigInteger.valueOf(1), BigInteger.valueOf(10), BigInteger.valueOf(20), BigInteger.valueOf(60)).send();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Response.ok(res);
    }

    @GetMapping("/stake-details")
    public Response getStakeDetails(@RequestParam String address) {
        return Response.ok(gethService.getStakeDetails(address));
    }

    @GetMapping("/coin/{coin}/price")
    public Response price(@PathVariable CoinService.CoinEnum coin) {
        return Response.ok(coin.getPrice());
    }

    @GetMapping("/coin/{coin}/sign")
    public Response sign(@PathVariable CoinService.CoinEnum coin, @RequestParam String fromAddress, @RequestParam String toAddress, @RequestParam BigDecimal amount) {
        return Response.ok(coin.sign(fromAddress, toAddress, amount));
    }

    @GetMapping("/coin/{coin}/submit")
    public Response submit(@PathVariable CoinService.CoinEnum coin, @RequestParam String hex) {
        SubmitTransactionDTO dto = new SubmitTransactionDTO();
        dto.setHex(hex);

        return Response.ok(coin.submitTransaction(dto));
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