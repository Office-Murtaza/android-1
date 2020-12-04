package com.belco.server.rest;

import com.belco.server.dto.PushNotificationDTO;
import com.belco.server.dto.SubmitTransactionDTO;
import com.belco.server.model.Response;
import com.belco.server.repository.CoinRep;
import com.belco.server.service.*;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.web3j.tuples.generated.Tuple2;
import wallet.core.jni.CoinType;

import java.math.BigDecimal;
import java.math.BigInteger;

@RestController
@RequestMapping("/api/v1/test")
public class TestController {

    @Autowired
    private TwilioService twilioService;

    @Autowired
    private PushNotificationService pushNotificationService;

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
            res.put("totalStakes", new BigDecimal(gethService.catm.totalStakes().send()).divide(GethService.ETH_DIVIDER));
            res.put("basePeriod(s)", gethService.catm.basePeriod().send());
            res.put("holdPeriod(s)", gethService.catm.holdPeriod().send());
            res.put("annualPercent", gethService.catm.annualPercent().send());
            res.put("annualPeriod(s)", gethService.catm.annualPeriod().send());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Response.ok(res);
    }

    @GetMapping("/stake-details")
    public Response getStakeDetails(@RequestParam String address) {
        JSONObject json = new JSONObject();
        json.put("isStakeholder", false);

        try {
            json.put("isStakeholder", gethService.catm.isStakeholder(address).send().component1());
        } catch (Exception e) {
        }

        try {
            json.put("amount", gethService.catm.stakeOf(address).send().intValue());
        } catch (Exception e) {
        }

        try {
            Tuple2<BigInteger, BigInteger> tuple2 = gethService.catm.stakeDetails(address).send();
            json.put("startDate", tuple2.component1());
            json.put("cancelDate", tuple2.component2());
        } catch (Exception e) {
        }

        return Response.ok(json);
    }

    @GetMapping("/push-notifications")
    public Response pushNotifications(@RequestParam String title, @RequestParam String message, @RequestParam String token) {
        String result = pushNotificationService.sendMessageToToken(new PushNotificationDTO(title, message, null, token));

        return Response.ok("result", result);
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