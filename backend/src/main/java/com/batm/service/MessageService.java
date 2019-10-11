package com.batm.service;

import com.batm.dto.SubmitTransactionDTO;
import com.batm.entity.CodeVerification;
import com.batm.entity.User;
import com.batm.repository.CodeVerificationRepository;
import com.batm.util.Constant;
import com.twilio.Twilio;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.annotation.PostConstruct;
import java.net.URI;
import java.time.Instant;
import java.util.Arrays;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

@Slf4j
@Service
public class MessageService {

    @Value("${twilio.mode}")
    private Integer twilioMode;

    @Value("${twilio.account-sid}")
    private String accountSid;

    @Value("${twilio.auth-token}")
    private String authToken;

    @Value("${twilio.from-number}")
    private String fromNumber;

    @Autowired
    private CodeVerificationRepository codeVerificationRepository;

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

            if (twilioMode == Constant.ENABLED) {
                code = RandomStringUtils.randomNumeric(4);
                status = sendMessage(user.getPhone(), "Belco Wallet Code: " + code);
            }

            CodeVerification codeVerification = codeVerificationRepository.findByUserUserId(user.getUserId());

            if (codeVerification == null) {
                codeVerification = new CodeVerification(user, code, "0");
            } else {
                codeVerification.setCodeStatus("0");
                codeVerification.setCode(code);
                codeVerification.setLastModifiedDate(Instant.now());
            }

            codeVerificationRepository.save(codeVerification);

            return status;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public Message.Status sendGiftMessage(CoinService.CoinEnum coinId, SubmitTransactionDTO dto, Boolean userExists) {
        try {
            StringBuilder body = new StringBuilder("You receive " + dto.getAmount() + " " + coinId.name()).append("\n").append(dto.getMessage());

            if (!userExists) {
                body.append("\n").append("In order to receive it install Belco Wallet app and create an account using your current phone number");
            }

            Message message = Message
                    .creator(new PhoneNumber(dto.getPhone()), new PhoneNumber(fromNumber), body.toString())
                    .setMediaUrl(Arrays.asList(URI.create(dto.getImage())))
                    .create();

            return message.getStatus();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}