package com.batm.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.batm.entity.Response;
import com.binance.api.client.BinanceApiRestClient;

@RestController
@RequestMapping("/api/v1")
public class BinanceController {

	@Autowired
	private BinanceApiRestClient binanceApiRestClient;

	@GetMapping("/binance/getcurrentprice")
	public Response addCoins(@RequestParam String coinCode) {
		return Response.ok(binanceApiRestClient.getPrice(coinCode+"USDT"));
	}

}
