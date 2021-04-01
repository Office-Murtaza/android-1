package com.belco.server.service;

import com.belco.server.dto.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class SocketService {

    private final SimpMessagingTemplate simpMessagingTemplate;

    public SocketService(SimpMessagingTemplate simpMessagingTemplate) {
        this.simpMessagingTemplate = simpMessagingTemplate;
    }

    public void pushBalance(String phone, BalanceDTO dto) {
        simpMessagingTemplate.convertAndSendToUser(phone, "/queue/balance", dto);
    }

    public void pushTransaction(String phone, TxDetailsDTO dto) {
        simpMessagingTemplate.convertAndSendToUser(phone, "/queue/transaction", dto);
    }

    public void pushTrade(TradeDTO dto) {
        simpMessagingTemplate.convertAndSend("/topic/trade", dto);
    }

    public void pushOrder(String phone, OrderDTO dto) {
        simpMessagingTemplate.convertAndSendToUser(phone, "/queue/order", dto);
    }

    public void pushChatMessage(String phone, ChatMessageDTO dto) {
        simpMessagingTemplate.convertAndSendToUser(phone, "/queue/order-chat", dto);
    }
}