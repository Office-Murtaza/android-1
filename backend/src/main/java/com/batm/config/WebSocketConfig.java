package com.batm.config;

import com.batm.entity.Token;
import com.batm.repository.TokenRep;
import com.batm.security.TokenProvider;
import com.batm.service.CoinService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.lang.Nullable;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.*;
import org.springframework.web.socket.server.HandshakeInterceptor;
import java.util.List;
import java.util.Map;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Autowired
    private TokenProvider tokenProvider;

    @Autowired
    private TokenRep tokenRep;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/api/v1/ws").addInterceptors(new HandshakeInterceptor() {

            @Override
            public boolean beforeHandshake(ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse, WebSocketHandler webSocketHandler, Map<String, Object> map) throws Exception {
                serverHttpRequest.getHeaders().setUpgrade("WebSocket");
                serverHttpRequest.getHeaders().setConnection("Upgrade");

                return true;
            }

            @Override
            public void afterHandshake(ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse, WebSocketHandler webSocketHandler, @Nullable Exception e) {
            }
        });
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/queue");
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {

            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

                if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                    if (accessor.getNativeHeader("Authorization") != null) {
                        List<String> authorization = accessor.getNativeHeader("Authorization");

                        String accessToken = authorization.get(0).split(" ")[1];

                        if (tokenProvider.validateToken(accessToken)) {
                            Authentication authentication = tokenProvider.getAuthentication(accessToken);

                            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                                SecurityContextHolder.getContext().setAuthentication(authentication);
                            }

                            accessor.setUser(authentication);

                            System.out.println(" ++++ connect: " + authentication.getName());

                            Token token = tokenRep.findByAccessToken(accessToken);
                            CoinService.wsMap.put(authentication.getName(), token.getUser().getId());
                        }
                    }
                } else if (StompCommand.DISCONNECT.equals(accessor.getCommand())) {
                    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                    String name = authentication == null ? accessor.getUser().getName() : authentication.getName();

                    System.out.println(" ---- disconnect: " + name);

                    CoinService.wsMap.remove(name);
                }

                return message;
            }
        });
    }
}