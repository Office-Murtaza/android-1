package com.belco.server.config;

import com.belco.server.entity.User;
import com.belco.server.security.JWTTokenProvider;
import com.belco.server.service.CoinService;
import com.belco.server.service.UserService;
import com.belco.server.util.Constant;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JWTTokenProvider tokenProvider;
    private final CoinService coinService;
    private final UserService userService;
    private final MessageChannel clientOutboundChannel;

    public WebSocketConfig(JWTTokenProvider tokenProvider, @Lazy CoinService coinService, @Lazy UserService userService, @Lazy @Qualifier("clientOutboundChannel") MessageChannel clientOutboundChannel) {
        this.tokenProvider = tokenProvider;
        this.coinService = coinService;
        this.userService = userService;
        this.clientOutboundChannel = clientOutboundChannel;
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/api/v1/ws").setAllowedOrigins("*");
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic", "/queue");
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {

            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

                if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                    if (accessor.getNativeHeader(Constant.AUTHORIZATION_HEADER) != null) {
                        List<String> authorization = accessor.getNativeHeader(Constant.AUTHORIZATION_HEADER);

                        if (authorization.size() > 0 && authorization.get(0).split(" ").length > 1) {
                            String accessToken = authorization.get(0).split(" ")[1];

                            if (tokenProvider.validateToken(accessToken)) {
                                Authentication authentication = tokenProvider.getAuthentication(accessToken);

                                if (SecurityContextHolder.getContext().getAuthentication() == null) {
                                    SecurityContextHolder.getContext().setAuthentication(authentication);
                                }

                                accessor.setUser(authentication);

                                System.out.println(" ---- CONNECT: " + authentication.getName());
                            } else {
                                StompHeaderAccessor headerAccessor = StompHeaderAccessor.create(StompCommand.ERROR);
                                headerAccessor.setMessage("Access is denied");
                                headerAccessor.setSessionId(accessor.getSessionId());
                                clientOutboundChannel.send(MessageBuilder.createMessage(new byte[0], headerAccessor.getMessageHeaders()));
                            }
                        }
                    }
                } else if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
                    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                    String name = authentication == null ? accessor.getUser().getName() : authentication.getName();

                    List<String> coinsHeader = accessor.getNativeHeader("coins");
                    List<String> coins = new ArrayList<>(Arrays.asList(coinsHeader.get(0).split("\\s*,\\s*")));

                    System.out.println(" ---- SUBSCRIBE: " + name);

                    User user = userService.findByPhone(name).get();
                    CoinService.wsMap.put(authentication.getName(), Collections.singletonMap(user.getId(), coins));
                    coinService.pushBalance(authentication.getName(), user.getId(), coins);
                } else if (StompCommand.DISCONNECT.equals(accessor.getCommand()) || StompCommand.UNSUBSCRIBE.equals(accessor.getCommand())) {
                    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                    String name = authentication == null ? accessor.getUser().getName() : authentication.getName();
                    System.out.println(" ---- " + accessor.getCommand() + ": " + name);

                    if (StringUtils.isNotBlank(name) && "anonymous".equalsIgnoreCase(name)) {
                        StompHeaderAccessor headerAccessor = StompHeaderAccessor.create(StompCommand.ERROR);
                        headerAccessor.setMessage("Access is denied");
                        headerAccessor.setSessionId(accessor.getSessionId());
                        clientOutboundChannel.send(MessageBuilder.createMessage(new byte[0], headerAccessor.getMessageHeaders()));
                    } else {
                        CoinService.wsMap.remove(name);
                    }
                }

                return message;
            }
        });
    }
}