package com.batm.rest;

import java.util.ArrayList;
import java.util.List;
import com.batm.util.Base58;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.batm.entity.Response;
import com.binance.api.client.BinanceApiRestClient;
import com.twilio.sdk.TwilioRestClient;
import com.twilio.sdk.TwilioRestException;
import com.twilio.sdk.resource.factory.MessageFactory;

@RestController
@RequestMapping("/api/v1/test")
public class TestController {

    @Autowired
    private BinanceApiRestClient binance;

    @Autowired
    private TwilioRestClient twilio;

    @Value("${twilio.fromNumber}")
    private String fromNumber;

    @GetMapping("/binance/price")
    public Response addCoins(@RequestParam String pair) {
        return Response.ok(binance.getPrice(pair));
    }

    @GetMapping("/twilio/send")
    public Response sendMessage(@RequestParam String phone) throws TwilioRestException {
        List<NameValuePair> params = new ArrayList<>();

        params.add(new BasicNameValuePair("To", phone));
        params.add(new BasicNameValuePair("From", fromNumber));
        params.add(new BasicNameValuePair("Body", "Code: " + RandomStringUtils.randomNumeric(4)));

        MessageFactory messageFactory = twilio.getAccount().getMessageFactory();

        return Response.ok(messageFactory.create(params).getStatus());
    }

    @GetMapping("/trx/toBase58")
    public Response toBase58(@RequestParam String hex) {
        return Response.ok(Base58.toBase58(hex));
    }

    @GetMapping("/trx/toHex")
    public Response toHex(@RequestParam String base58) {
        return Response.ok(Base58.toHex(base58));
    }
}