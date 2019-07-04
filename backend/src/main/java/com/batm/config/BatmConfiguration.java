package com.batm.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.binance.api.client.BinanceApiClientFactory;
import com.binance.api.client.BinanceApiRestClient;

@Configuration
public class BatmConfiguration {

	@Value("${binance.api-key}")
	private String binanceApiKey;

	@Value("${binance.secret-key}")
	private String binanceSecretKey;

	@Bean
	public BinanceApiRestClient binanceApiRestClient() {
		BinanceApiClientFactory factory = BinanceApiClientFactory.newInstance(binanceApiKey, binanceSecretKey);
		BinanceApiRestClient client = factory.newRestClient();
		return client;
	}

}
