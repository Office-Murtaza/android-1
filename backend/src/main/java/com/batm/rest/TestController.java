package com.batm.rest;

import com.batm.service.ChainalysisService;
import com.batm.service.MessageService;
import com.batm.service.TransactionService;
import com.batm.service.WalletService;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.batm.model.Response;

@RestController
@RequestMapping("/api/v1/test")
public class TestController {

    @Autowired
    private MessageService messageService;

    @Autowired
    private WalletService walletService;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private ChainalysisService chainalysisService;

    @GetMapping("/sms")
    public Response sendSMS(@RequestParam String phone) {
        return Response.ok(messageService.sendMessage(phone, "Hey there, this is a test message!!!"));
    }

    @GetMapping("/wallet")
    public Response getWalletAddresses() {
        JSONObject res = new JSONObject();
        res.put("BTC", walletService.getAddressBTC());
        res.put("BCH", walletService.getAddressBCH());
        res.put("ETH", walletService.getAddressETH());
        res.put("LTC", walletService.getAddressLTC());
        res.put("BNB", walletService.getAddressBNB());
        res.put("XRP", walletService.getAddressXRP());
        res.put("TRX", walletService.getAddressTRX());

        return Response.ok(res);
    }

    @GetMapping("/gifts")
    public Response gifts() {
        transactionService.processCronTasks();

        return Response.ok(true);
    }

    @GetMapping("/chainalysis")
    public Response chainalysis() {
        chainalysisService.processUntrackedTransactions();

        return Response.ok(true);
    }
}