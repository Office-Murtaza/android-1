package system.service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.springframework.stereotype.Service;

@Service
public class TwilioService {

    public static final String ACCOUNT_SID = "ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX";
    public static final String AUTH_TOKEN = "your_auth_token";
    public static final String senderNumber = "23423432";
    public static final String sendCodeMessage = "Your code is %s";

    public void sendCode(String recepientNumber, String code) {
        String messageWithCode = String.format(sendCodeMessage, code);
        System.out.println("Message with code is " + messageWithCode);
//        sendMessage(senderNumber, recepientNumber, messageWithCode);//todo uncomment when set up Twilio
    }

    private void sendMessage(String senderNumber, String recepientNumber, String messageText) {
        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
        Message message = Message.creator(new PhoneNumber(recepientNumber),
                new PhoneNumber(senderNumber),
                messageText).create();
        System.out.println("Twilio message sent: " + message.getSid());
    }
}
