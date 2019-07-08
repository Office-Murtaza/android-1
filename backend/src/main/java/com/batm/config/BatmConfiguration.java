package com.batm.config;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.binance.api.client.BinanceApiClientFactory;
import com.binance.api.client.BinanceApiRestClient;

@EnableJpaRepositories("com.batm")
@EnableTransactionManagement
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

	@Bean
	public AuditorAware<String> auditorProvider() {
		return () -> Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication().getName());
	}

}
