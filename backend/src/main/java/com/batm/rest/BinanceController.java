package com.batm.rest;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.batm.entity.Response;
import com.batm.repository.CodeVerificationRepository;
import com.binance.api.client.BinanceApiRestClient;
import com.twilio.sdk.TwilioRestClient;
import com.twilio.sdk.TwilioRestException;
import com.twilio.sdk.resource.factory.MessageFactory;
import com.twilio.sdk.resource.instance.Message;

@RestController
@RequestMapping("/api/v1")
public class BinanceController {

	@Autowired
	private BinanceApiRestClient binanceApiRestClient;
	

	@Autowired
	private TwilioRestClient twilioRestClient;

	@Value("${twilio.fromNumber}")
	private String fromNumber;

	@GetMapping("/binance/getcurrentprice")
	public Response addCoins(@RequestParam String coinCode) {
		return Response.ok(binanceApiRestClient.getPrice(coinCode+"USDT"));
	}

	@GetMapping("/twillio/send")
	public Response sendMessage(@RequestParam String phone) throws TwilioRestException {
		List<NameValuePair> params = new ArrayList<>();
		String phoneNumber = phone;
		params.add(new BasicNameValuePair("To", phoneNumber));
		params.add(new BasicNameValuePair("From", fromNumber));
		params.add(new BasicNameValuePair("Body",
				"Dear Customer, How are you?."));

		MessageFactory messageFactory = twilioRestClient.getAccount().getMessageFactory();
		Message message = messageFactory.create(params);
		return Response.ok("send");
	}
}
