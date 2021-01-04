package com.belco.server.rest;

import com.belco.server.dto.NotificationDTO;
import com.belco.server.dto.SubmitTransactionDTO;
import com.belco.server.model.Response;
import com.belco.server.repository.CoinRep;
import com.belco.server.service.*;
import net.sf.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.web3j.tuples.generated.Tuple2;
import wallet.core.jni.CoinType;

import java.math.BigDecimal;
import java.math.BigInteger;

@RestController
@RequestMapping("/api/v1/test")
public class TestController {

    private final TwilioService twilioService;
    private final NotificationService pushNotificationService;
    private final UserService userService;
    private final WalletService walletService;
    private final GethService gethService;
    private final NodeService nodeService;
    private final CoinRep coinRep;

    public TestController(TwilioService twilioService, NotificationService pushNotificationService, UserService userService, WalletService walletService, GethService gethService, NodeService nodeService, CoinRep coinRep) {
        this.twilioService = twilioService;
        this.pushNotificationService = pushNotificationService;
        this.userService = userService;
        this.walletService = walletService;
        this.gethService = gethService;
        this.nodeService = nodeService;
        this.coinRep = coinRep;
    }

    @GetMapping("/send-sms")
    public Response sendSMS(@RequestParam String phone) {
        return Response.ok(twilioService.sendMessage(phone, "This is a test message"));
    }

    @GetMapping("/wallet-details")
    public Response getWalletDetails() {
        JSONObject res = new JSONObject();

        coinRep.findAllByOrderByIdxAsc().stream().forEach(e -> {
            CoinService.CoinEnum coinEnum = CoinService.CoinEnum.valueOf(e.getCode());
            CoinType coinType = coinEnum.getCoinType();

            res.put(e.getCode(), getCoinJson(walletService.getCoinsMap().get(coinType).getAddress(), coinEnum.getBalance(walletService.getCoinsMap().get(coinType).getAddress()), coinEnum.getTxFee(), nodeService.getNodeUrl(coinType)));
        });

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

        try {
            json.put("totalStakes", new BigDecimal(gethService.catm.totalStakes().send()).divide(GethService.ETH_DIVIDER));
        } catch (Exception e) {
        }

        try {
            json.put("basePeriod(s)", gethService.catm.basePeriod().send());
        } catch (Exception e) {
        }

        try {
            json.put("holdPeriod(s)", gethService.catm.holdPeriod().send());
        } catch (Exception e) {
        }

        try {
            json.put("annualPercent", gethService.catm.annualPercent().send());
        } catch (Exception e) {
        }

        try {
            json.put("annualPeriod(s)", gethService.catm.annualPeriod().send());
        } catch (Exception e) {
        }

        return Response.ok(json);
    }

    @GetMapping("/send-notification")
    public Response sendNotification(@RequestParam(required = false) Long userId, @RequestParam(required = false) String token, @RequestParam String title, @RequestParam String message) {
        if (StringUtils.isBlank(token)) token = userService.findById(userId).getNotificationsToken();

        return Response.ok("result", pushNotificationService.sendMessageWithData(new NotificationDTO(title, message, null, token)));
    }

    @GetMapping("/user/{userId}/delete-verification")
    public Response deleteUserVerification(@PathVariable Long userId) {
        return Response.ok(userService.deleteKyc(userId));
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

    private JSONObject getCoinJson(String address, BigDecimal balance, BigDecimal fee, String nodeUrl) {
        JSONObject json = new JSONObject();
        json.put("address", address);
        json.put("balance", balance.toPlainString());
        json.put("fee", fee.toPlainString());
        json.put("nodeUrl", nodeUrl);

        return json;
    }
}