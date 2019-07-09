package com.batm.config;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.twilio.sdk.TwilioRestClient;
import com.twilio.sdk.TwilioRestException;
import com.twilio.sdk.resource.factory.MessageFactory;
import com.twilio.sdk.resource.instance.Message;

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
	
	
	public static void main(String[] args) throws TwilioRestException {
		TwilioRestClient client = new TwilioRestClient("AC7a1ae375d456dc5adca6570e538e0d04","f80ee19779216a604394cfc6f9674591");
		
		List<NameValuePair> params = new ArrayList<>();
		params.add(new BasicNameValuePair("To", "+16466756302"));
		params.add(new BasicNameValuePair("From", "+12018906708"));
		params.add(new BasicNameValuePair("Body",
				"Dear Customer, How are you?"));

		MessageFactory messageFactory = client.getAccount().getMessageFactory();
		Message message = messageFactory.create(params);
		System.out.println(message);
	}
}