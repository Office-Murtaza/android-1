package com.batm.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.batm.entity.CodeVerification;
import com.batm.entity.User;
import com.batm.repository.CodeVerificationRepository;
import com.twilio.sdk.TwilioRestClient;
import com.twilio.sdk.TwilioRestException;
import com.twilio.sdk.resource.factory.MessageFactory;
import com.twilio.sdk.resource.instance.Message;

@Component
public class TwilioComponent {

	private final Logger log = LoggerFactory.getLogger(TwilioComponent.class);

	@Autowired
	private TwilioRestClient twilioRestClient;

	@Autowired
	private CodeVerificationRepository codeVerificationRepository;

	@Value("${twilio.fromNumber}")
	private String fromNumber;

	@Value("${server.mode}")
	private Integer serverMode;

	public void sendOTP(User user) {

		try {
			String otp = "";
			if (serverMode == 0) {
				otp = "1234";
			} else {
				otp = RandomStringUtils.randomNumeric(4);
				// Build the parameters
				List<NameValuePair> params = new ArrayList<>();
				String phoneNumber = user.getPhone();
				params.add(new BasicNameValuePair("To", phoneNumber));
				params.add(new BasicNameValuePair("From", fromNumber));
				params.add(new BasicNameValuePair("Body",
						"Dear Customer, " + otp + " is your one time password(OTP).Please enter the OTP to proceed."));

				MessageFactory messageFactory = twilioRestClient.getAccount().getMessageFactory();
				Message message = messageFactory.create(params);
				log.info("msg sid {}", message.getSid());
			}
			
			codeVerificationRepository.save(new CodeVerification(user, otp, "0"));

		} catch (TwilioRestException e) {
			log.error("Getting error while sending message", e);
		}

	}

}
