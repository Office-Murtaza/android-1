package com.batm.service;

import com.batm.dto.SubmitTransactionDTO;
import com.batm.entity.CodeVerify;
import com.batm.entity.User;
import com.batm.repository.CodeVerifyRep;
import com.batm.util.Constant;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.MessageCreator;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.annotation.PostConstruct;
import java.net.URI;
import java.util.Arrays;
import java.util.Date;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

@Service
public class MessageService {

    @Value("${twilio.enabled}")
    private Boolean twilioEnabled;

    @Value("${twilio.account-sid}")
    private String accountSid;

    @Value("${twilio.auth-token}")
    private String authToken;

    @Value("${twilio.from-number}")
    private String fromNumber;

    @Autowired
    private CodeVerifyRep codeVerifyRep;

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
            e.printStackTrace();
        }

        return null;
    }

    public Message.Status sendVerificationCode(User user) {
        try {
            Message.Status status = null;
            String code = Constant.DEFAULT_CODE;

            if (twilioEnabled) {
                code = RandomStringUtils.randomNumeric(4);
                status = sendMessage(user.getPhone(), "Code: " + code);
            }

            CodeVerify codeVerify = codeVerifyRep.findByUserId(user.getId());

            if (codeVerify == null) {
                codeVerify = new CodeVerify();
                codeVerify.setUser(user);
                codeVerify.setCode(code);
                codeVerify.setStatus(0);
            } else {
                codeVerify.setStatus(0);
                codeVerify.setCode(code);
            }

            codeVerify.setUpdateDate(new Date());

            codeVerifyRep.save(codeVerify);

            return status;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public String getVerificationCode(Long userId) {
        try {
            return codeVerifyRep.findByUserId(userId).getCode();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public Message.Status sendGiftMessage(CoinService.CoinEnum coinCode, SubmitTransactionDTO dto, boolean receiverExists) {
        try {
            StringBuilder body = new StringBuilder();

            if (StringUtils.isNotBlank(dto.getMessage())) {
                body.append("\"").append(dto.getMessage()).append("\"").append("\n");
            }

            body.append("\n").append("Congrats, you've just received " + dto.getCryptoAmount() + " " + coinCode.name() + " gift");

            if (!receiverExists) {
                body.append("\n\n").append("To receive it, install Belco Wallet from a link");
                body.append("\n\n").append("IOS:");
                body.append("\n").append(Constant.APP_LINK_IOS);
                body.append("\n\n").append("Android:");
                body.append("\n").append(Constant.APP_LINK_ANDROID);
                body.append("\n\n").append("and create an account using " + dto.getPhone() + " number");
                body.append("\n");
            }

            MessageCreator messageCreator = Message.creator(new PhoneNumber(dto.getPhone()), new PhoneNumber(fromNumber), body.toString());

            if (StringUtils.isNotBlank(dto.getImageId())) {
                messageCreator.setMediaUrl(Arrays.asList(URI.create("https://media.giphy.com/media/" + dto.getImageId().trim() + "/giphy.gif")));
            }

            Message message = messageCreator.create();

            return message.getStatus();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}