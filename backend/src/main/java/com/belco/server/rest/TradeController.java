package com.belco.server.rest;

import com.belco.server.dto.OrderDTO;
import com.belco.server.dto.OrderMessageDTO;
import com.belco.server.dto.TradeDTO;
import com.belco.server.model.Response;
import com.belco.server.service.TradeService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/")
public class TradeController {

    private final TradeService tradeService;

    public TradeController(TradeService tradeService) {
        this.tradeService = tradeService;
    }

    @GetMapping("/user/{userId}/trade-history")
    public Response getTradeHistory(@PathVariable Long userId) {
        return tradeService.getTradeHistory(userId);
    }

    @PostMapping("/user/{userId}/trade")
    public Response createTrade(@PathVariable Long userId, @RequestBody TradeDTO dto) {
        return tradeService.createTrade(userId, dto);
    }

    @PutMapping("/user/{userId}/trade")
    public Response updateTrade(@PathVariable Long userId, @RequestBody TradeDTO dto) {
        return tradeService.updateTrade(userId, dto);
    }

    @DeleteMapping("/user/{userId}/trade")
    public Response cancelTrade(@PathVariable Long userId, @RequestParam String id) {
        return tradeService.cancelTrade(userId, id);
    }

    @PostMapping("/user/{userId}/order")
    public Response createOrder(@PathVariable Long userId, @RequestBody OrderDTO dto) {
        return tradeService.createOrder(userId, dto);
    }

    @PutMapping("/user/{userId}/order")
    public Response updateOrder(@PathVariable Long userId, @RequestBody OrderDTO dto) {
        return tradeService.updateOrder(userId, dto);
    }

    @DeleteMapping("/user/{userId}/order")
    public Response cancelOrder(@PathVariable Long userId, @RequestParam String id) {
        return tradeService.cancelOrder(userId, id);
    }

    @MessageMapping("/order-chat")
    public void processMessage(@Payload OrderMessageDTO dto) {
        tradeService.processMessage(dto);
    }
}