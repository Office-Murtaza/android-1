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

    public void pushTransaction(String phone, TransactionDetailsDTO dto) {
        simpMessagingTemplate.convertAndSendToUser(phone, "/queue/transaction", dto);
    }

    public void pushTrade(TradeDetailsDTO dto) {
        simpMessagingTemplate.convertAndSend("/topic/trade", dto);
    }

    public void pushOrder(String phone, OrderDetailsDTO dto) {
        simpMessagingTemplate.convertAndSendToUser(phone, "/queue/order", dto);
    }

    public void pushChatMessage(String phone, OrderMessageDTO dto) {
        simpMessagingTemplate.convertAndSendToUser(phone, "/queue/order-chat", dto);
    }
}