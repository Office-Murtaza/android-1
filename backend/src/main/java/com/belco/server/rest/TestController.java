package com.belco.server.rest;

import com.belco.server.dto.NotificationDTO;
import com.belco.server.dto.TransactionDTO;
import com.belco.server.dto.TransactionDetailsDTO;
import com.belco.server.model.Response;
import com.belco.server.repository.CoinRep;
import com.belco.server.service.*;
import net.sf.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tuples.generated.Tuple2;
import wallet.core.jni.CoinType;

import java.math.BigDecimal;
import java.math.BigInteger;

@RestController
@RequestMapping("/api/v1/test")
public class TestController {

    private final TwilioService twilioService;
    private final NotificationService notificationService;
    private final UserService userService;
    private final WalletService walletService;
    private final GethService gethService;
    private final NodeService nodeService;
    private final CoinRep coinRep;
    private final SocketService socketService;

    public TestController(TwilioService twilioService, NotificationService notificationService, UserService userService, WalletService walletService, GethService gethService, NodeService nodeService, CoinRep coinRep, SocketService socketService) {
        this.twilioService = twilioService;
        this.notificationService = notificationService;
        this.userService = userService;
        this.walletService = walletService;
        this.gethService = gethService;
        this.nodeService = nodeService;
        this.coinRep = coinRep;
        this.socketService = socketService;
    }

    @GetMapping("/push-transaction")
    public Response pushTransaction(@RequestParam String phone) {
        TransactionDetailsDTO dto = new TransactionDetailsDTO();
        dto.setTxId("0xca05a14dfa36e0474f3466c6fba2d1996639d88bbff6a8a7a7bc55242fe60571");

        socketService.pushTransaction(phone, dto);

        return Response.ok(true);
    }

    @GetMapping("/send-sms")
    public Response sendSMS(@RequestParam String phone) {
        return Response.ok(twilioService.sendMessage(phone, "This is a test message"));
    }

    @GetMapping("/wallet/{walletId}/details")
    public Response getWalletDetails(@PathVariable Long walletId) {
        JSONObject res = new JSONObject();

        coinRep.findAllByOrderByIdxAsc().stream().forEach(e -> {
            CoinService.CoinEnum coinEnum = CoinService.CoinEnum.valueOf(e.getCode());
            CoinType coinType = coinEnum.getCoinType();
            String address = walletService.get(walletId).getCoins().get(coinType).getAddress();

            res.put(e.getCode(), getCoinJson(address, coinEnum.getBalance(address), coinEnum.getTxFee(), nodeService.getNodeUrl(coinType)));
        });

        return Response.ok(res);
    }

    @GetMapping("/block-details")
    public Response getBlockDetails(@RequestParam Integer blockNumber) {
        gethService.processBlock(blockNumber);

        return Response.ok(true);
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

        json.put("basePeriod(s)", gethService.getStakingBasePeriod());
        json.put("holdPeriod(s)", gethService.getStakingHoldPeriod());
        json.put("annualPeriod(s)", gethService.getStakingAnnualPeriod());
        json.put("annualPercent", gethService.getStakingAnnualPercent());

        return Response.ok(json);
    }

    @GetMapping("/gas-limit")
    public Response getGasLimit(@RequestParam String toAddress) {
        try {
            return Response.ok("result", gethService.web3.ethEstimateGas(org.web3j.protocol.core.methods.request.Transaction.createEthCallTransaction(null, toAddress, null)).send().getAmountUsed().longValue());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Response.ok("result", "error");
    }

    @GetMapping("/send-notification")
    public Response sendNotification(@RequestParam(required = false) Long userId, @RequestParam(required = false) String token, @RequestParam String title, @RequestParam String message) {
        if (StringUtils.isBlank(token)) token = userService.findById(userId).getNotificationsToken();

        return Response.ok("result", notificationService.sendMessageWithData(new NotificationDTO(title, message, null, token)));
    }

    @GetMapping("/user/{userId}/delete-verification")
    public Response deleteUserVerification(@PathVariable Long userId) {
        return Response.ok(userService.deleteVerification(userId));
    }

    @GetMapping("/wallet/{walletId}/coin/{coin}/sign")
    public Response sign(@PathVariable Long walletId, @PathVariable CoinService.CoinEnum coin, @RequestParam String fromAddress, @RequestParam String toAddress, @RequestParam BigDecimal amount) {
        return Response.ok(coin.sign(walletId, fromAddress, toAddress, amount));
    }

    @GetMapping("/coin/{coin}/submit")
    public Response submit(@PathVariable CoinService.CoinEnum coin, @RequestParam String fromAddress, @RequestParam String toAddress, @RequestParam BigDecimal amount, @RequestParam String hex) {
        TransactionDTO dto = new TransactionDTO();
        dto.setFromAddress(fromAddress);
        dto.setToAddress(toAddress);
        dto.setCryptoAmount(amount);
        dto.setHex(hex);

        return Response.ok(coin.submitTransaction(dto));
    }

    @GetMapping("/transaction-receipt")
    public Response getTransactionReceipt(@RequestParam String txId) {
        try {
            TransactionReceipt receipt = gethService.web3.ethGetTransactionReceipt(txId).send().getTransactionReceipt().get();

            return Response.ok(receipt);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Response.ok("error");
    }

    @GetMapping("/{coin}/transaction-details")
    public Response getTransactionDetails(@PathVariable CoinService.CoinEnum coin, @RequestParam String txId, @RequestParam String address) {
        try {
            TransactionDetailsDTO tx = coin.getTransactionDetails(txId, address);

            return Response.ok(tx);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Response.ok("error");
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