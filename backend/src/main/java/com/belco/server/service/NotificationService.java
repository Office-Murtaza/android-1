package com.belco.server.service;

import com.belco.server.dto.NotificationDTO;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.time.Duration;

import com.google.firebase.messaging.AndroidConfig;
import com.google.firebase.messaging.AndroidNotification;
import com.google.firebase.messaging.ApnsConfig;
import com.google.firebase.messaging.Aps;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;

@Service
public class NotificationService {

    private final static String TITLE_KEY = "title";
    private final static String MESSAGE_KEY = "message";

    @Value("${app.firebase.project-name}")
    private String projectName;

    @Value("${app.firebase.configuration-file}")
    private String configurationFile;

    @PostConstruct
    public void init() {
        try {
            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(new ClassPathResource(configurationFile).getInputStream()))
                    .setDatabaseUrl("https://" + projectName + ".firebaseio.com")
                    .build();

            FirebaseApp.initializeApp(options);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String sendMessageWithData(Map<String, String> data, NotificationDTO dto) {
        try {
            return sendAndGetResponse(getPreconfiguredMessageWithData(data, dto));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public String sendMessageToTopic(NotificationDTO dto) {
        try {
            return sendAndGetResponse(getPreconfiguredMessageToTopic(dto));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public String sendMessageToToken(NotificationDTO dto) {
        try {
            return sendAndGetResponse(getPreconfiguredMessageToToken(dto));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private String sendAndGetResponse(Message message) throws InterruptedException, ExecutionException {
        return FirebaseMessaging.getInstance().sendAsync(message).get();
    }

    private Message getPreconfiguredMessageToTopic(NotificationDTO dto) {
        return getPreconfiguredMessageBuilder(dto).setTopic(dto.getTopic())
                .build();
    }

    private Message getPreconfiguredMessageToToken(NotificationDTO dto) {
        return getPreconfiguredMessageBuilder(dto).setToken(dto.getToken())
                .build();
    }

    private Message getPreconfiguredMessageWithData(Map<String, String> data, NotificationDTO dto) {
        return getPreconfiguredMessageBuilder(dto).putAllData(data).setToken(dto.getToken())
                .build();
    }

    private Message.Builder getPreconfiguredMessageBuilder(NotificationDTO dto) {
        return Message.builder()
                .putData(TITLE_KEY, dto.getTitle())
                .putData(MESSAGE_KEY, dto.getMessage())
                .setNotification(new Notification(dto.getTitle(), dto.getMessage()));
    }
}