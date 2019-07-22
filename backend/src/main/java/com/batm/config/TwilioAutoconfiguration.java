package com.batm.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.twilio.sdk.TwilioRestClient;

@Configuration
@EnableConfigurationProperties(TwilioProperties.class)
public class TwilioAutoconfiguration {

    private final TwilioProperties properties;

    @Autowired
    public TwilioAutoconfiguration(TwilioProperties properties) {
        this.properties = properties;
    }

    @Bean
    public TwilioRestClient twilioRestClient() {
        return new TwilioRestClient(properties.getAccountSID(), properties.getAuthToken());
    }
}