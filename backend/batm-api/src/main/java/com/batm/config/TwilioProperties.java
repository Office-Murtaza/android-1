package com.batm.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author vikas
 *
 */

@ConfigurationProperties(prefix = "twilio")
public class TwilioProperties {

	private String accountSID;
	private String authToken;
	private String endpoint;

	public String getAccountSID() {
		return accountSID;
	}

	public void setAccountSID(String accountSID) {
		this.accountSID = accountSID;
	}

	public String getAuthToken() {
		return authToken;
	}

	public void setAuthToken(String authToken) {
		this.authToken = authToken;
	}

	public String getEndpoint() {
		return endpoint;
	}

	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}

}
