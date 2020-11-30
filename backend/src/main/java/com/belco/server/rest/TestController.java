package com.belco.server.rest;

import com.belco.server.dto.SubmitTransactionDTO;
import com.belco.server.model.Response;
import com.belco.server.repository.CoinRep;
import com.belco.server.service.*;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
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
    private GethService gethService;

    @Autowired
    private CoinRep coinRep;

    @GetMapping("/sms")
    public Response sendSMS(@RequestParam String phone) {
        return Response.ok(twilioService.sendMessage(phone, "This is a test message"));
    }

    @GetMapping("/wallet")
    public Response getWalletAddresses() {
        JSONObject res = new JSONObject();

        coinRep.findAllByOrderByIdxAsc().stream().forEach(e -> {
            CoinService.CoinEnum coinEnum = CoinService.CoinEnum.valueOf(e.getCode());
            CoinType coinType = coinEnum.getCoinType();

            res.put(e.getCode(), getCoinJson(walletService.getCoinsMap().get(coinType).getAddress(), coinEnum.getBalance(walletService.getCoinsMap().get(coinType).getAddress())));
        });

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

    private JSONObject getCoinJson(String address, BigDecimal balance) {
        JSONObject json = new JSONObject();

        json.put("address", address);
        json.put("balance", balance);

        return json;
    }
}