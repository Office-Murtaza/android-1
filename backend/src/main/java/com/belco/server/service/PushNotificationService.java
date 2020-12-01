package com.belco.server.service;

import com.belco.server.dto.PushNotificationDTO;
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
public class PushNotificationService {

    private final static String SOUND = "default";
    private final static String COLOR = "#FFFF00";

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

    public String sendMessageWithData(Map<String, String> data, PushNotificationDTO dto) {
        try {
            return sendAndGetResponse(getPreconfiguredMessageWithData(data, dto));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public String sendMessageToTopic(PushNotificationDTO dto) {
        try {
            return sendAndGetResponse(getPreconfiguredMessageToTopic(dto));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public String sendMessageToToken(PushNotificationDTO dto) {
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

    private AndroidConfig getAndroidConfig(String topic) {
        return AndroidConfig.builder()
                .setTtl(Duration.ofMinutes(2).toMillis()).setCollapseKey(topic)
                .setPriority(AndroidConfig.Priority.HIGH)
                .setNotification(AndroidNotification.builder().setSound(SOUND)
                        .setColor(COLOR).setTag(topic).build()).build();
    }

    private ApnsConfig getApnsConfig(String topic) {
        return ApnsConfig.builder()
                .setAps(Aps.builder().setCategory(topic).setThreadId(topic).build()).build();
    }

    private Message getPreconfiguredMessageToTopic(PushNotificationDTO dto) {
        return getPreconfiguredMessageBuilder(dto).setTopic(dto.getTopic())
                .build();
    }

    private Message getPreconfiguredMessageToToken(PushNotificationDTO dto) {
        return getPreconfiguredMessageBuilder(dto).setToken(dto.getToken())
                .build();
    }

    private Message getPreconfiguredMessageWithData(Map<String, String> data, PushNotificationDTO dto) {
        return getPreconfiguredMessageBuilder(dto).putAllData(data).setToken(dto.getToken())
                .build();
    }

    private Message.Builder getPreconfiguredMessageBuilder(PushNotificationDTO dto) {
        AndroidConfig androidConfig = getAndroidConfig(dto.getTopic());
        ApnsConfig apnsConfig = getApnsConfig(dto.getTopic());

        return Message.builder()
                .setApnsConfig(apnsConfig).setAndroidConfig(androidConfig).setNotification(
                        new Notification(dto.getTitle(), dto.getMessage()));
    }
}