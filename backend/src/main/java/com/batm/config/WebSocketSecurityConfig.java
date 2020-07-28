package com.batm.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.messaging.MessageSecurityMetadataSourceRegistry;
import org.springframework.security.config.annotation.web.socket.AbstractSecurityWebSocketMessageBrokerConfigurer;
import static org.springframework.messaging.simp.SimpMessageType.*;

@Configuration
public class WebSocketSecurityConfig extends AbstractSecurityWebSocketMessageBrokerConfigurer {

    @Override
    protected void configureInbound(MessageSecurityMetadataSourceRegistry messages) {
        messages.simpTypeMatchers(CONNECT, UNSUBSCRIBE, DISCONNECT).permitAll();
        messages.simpTypeMatchers(SUBSCRIBE).authenticated();
    }

    @Override
    protected boolean sameOriginDisabled() {
        return true;
    }
}