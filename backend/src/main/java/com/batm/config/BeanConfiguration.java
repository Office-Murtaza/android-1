package com.batm.config;

import com.binance.api.client.BinanceApiClientFactory;
import com.binance.api.client.BinanceApiRestClient;
import com.binance.dex.api.client.BinanceDexApiClientFactory;
import com.binance.dex.api.client.BinanceDexApiRestClient;
import com.binance.dex.api.client.BinanceDexEnvironment;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import java.util.Optional;

@Configuration
public class BeanConfiguration {

    @Value("${binance.api-key}")
    private String binanceApiKey;

    @Value("${binance.secret-key}")
    private String binanceSecretKey;

    @Bean
    public RestTemplate getRestTemplate() {
        return new RestTemplate();
    }

    @Bean
    public BinanceDexApiRestClient getBinanceDexApiRestClient() {
        return BinanceDexApiClientFactory.newInstance().newRestClient(BinanceDexEnvironment.PROD.getBaseUrl());
    }

    @Bean
    public BinanceApiRestClient getBinanceApiRestClient() {
        BinanceApiClientFactory factory = BinanceApiClientFactory.newInstance(binanceApiKey, binanceSecretKey);
        BinanceApiRestClient client = factory.newRestClient();
        return client;
    }

    @Bean
    public AuditorAware<String> getAuditorAware() {
        return () -> Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication().getName());
    }

    @Bean(name = "multipartResolver")
    public CommonsMultipartResolver multipartResolver() {
        CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver();
        multipartResolver.setMaxUploadSize(10_000_000); // 10mb
        return multipartResolver;
    }
}