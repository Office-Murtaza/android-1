package com.belco.server.service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.rest.api.v2010.account.MessageCreator;
import com.twilio.type.PhoneNumber;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.net.URI;
import java.util.Arrays;

@Service
public class TwilioService {

    private static final String APP_LINK_IOS = "itms-apps://itunes.apple.com/app/apple-store/id1475407885";
    private static final String APP_LINK_ANDROID = "https://play.google.com/store/apps/details?id=com.app.belcobtm";
    private static final String DEFAULT_CODE = "1234";

    @Value("${twilio.enabled}")
    private Boolean enabled;

    @Value("${twilio.account-sid}")
    private String accountSid;

    @Value("${twilio.auth-token}")
    private String authToken;

    @Value("${twilio.from-number}")
    private String fromNumber;

    @PostConstruct
    public void init() {
        Twilio.init(accountSid, authToken);
    }

    public Message.Status sendMessage(String phone, String text) {
        try {
            Message message = Message
                    .creator(new PhoneNumber(phone), new PhoneNumber(fromNumber), text)
                    .create();

            return message.getStatus();
        } catch (Exception e) {
        }

        return Message.Status.FAILED;
    }

    public String sendVerificationCode(String phone) {
        try {
            if (enabled) {
                String code = RandomStringUtils.randomNumeric(4);
                Message.Status status = sendMessage(phone, "Code: " + code);

                if (status == Message.Status.QUEUED) {
                    return code;
                }
            } else {
                return DEFAULT_CODE;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public Message.Status sendTransferMessageToNotExistingUser(CoinService.CoinEnum coinCode, String phone, String message, String imageId, BigDecimal amount) {
        try {
            StringBuilder messageBuilder = new StringBuilder("You just received " + amount.toPlainString() + " " + coinCode.name());

            if (StringUtils.isNotBlank(message)) {
                messageBuilder.append("\n\n").append("\"").append(message).append("\"").append("\n");
            }

            messageBuilder.append("\n\n").append("To get it, please install our app from a link");
            messageBuilder.append("\n\n").append("IOS:");
            messageBuilder.append("\n").append(APP_LINK_IOS);
            messageBuilder.append("\n\n").append("Android:");
            messageBuilder.append("\n").append(APP_LINK_ANDROID);
            messageBuilder.append("\n\n").append("and create an account using " + phone + " number");
            messageBuilder.append("\n");

            MessageCreator messageCreator = Message.creator(new PhoneNumber(phone), new PhoneNumber(fromNumber), messageBuilder.toString());

            if (StringUtils.isNotBlank(imageId)) {
                messageCreator.setMediaUrl(Arrays.asList(URI.create("https://media.giphy.com/media/" + imageId + "/giphy.gif")));
            }

            return messageCreator.create().getStatus();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Message.Status.FAILED;
    }
}