package com.belco.server;

import com.binance.dex.api.client.BinanceDexApiClientFactory;
import com.binance.dex.api.client.BinanceDexApiRestClient;
import com.binance.dex.api.client.BinanceDexEnvironment;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.filter.AbstractRequestLoggingFilter;

import javax.servlet.http.HttpServletRequest;

@EnableCaching
@SpringBootApplication
public class Main {

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    @Bean
    public RestTemplate getRestTemplate() {
        return new RestTemplate();
    }

    @Bean
    public BinanceDexApiRestClient getBinanceDexApiRestClient() {
        return BinanceDexApiClientFactory.newInstance().newRestClient(BinanceDexEnvironment.PROD.getBaseUrl());
    }

    @Component
    public class RestRequestLoggingFilter extends AbstractRequestLoggingFilter {

        private volatile long beforeRequestTimestamp;

        public RestRequestLoggingFilter() {
            setIncludeClientInfo(false);
            setIncludeHeaders(false);
            setIncludePayload(true);
            setIncludeQueryString(true);
            setAfterMessagePrefix("Request -> ");
        }

        @Override
        protected void beforeRequest(HttpServletRequest request, String message) {
            beforeRequestTimestamp = System.currentTimeMillis();
        }

        @Override
        protected void afterRequest(HttpServletRequest request, String message) {
            logger.info(message + ", duration: " + (System.currentTimeMillis() - beforeRequestTimestamp));
        }
    }
}