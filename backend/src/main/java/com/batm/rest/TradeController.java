package com.batm.rest;

import com.batm.dto.TradeDTO;
import com.batm.dto.TradeRequestDTO;
import com.batm.model.Response;
import com.batm.service.CoinService;
import com.batm.service.TradeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class TradeController {

    @Autowired
    private TradeService tradeService;

    @PostMapping("/user/{userId}/coins/{coinCode}/trade")
    public Response postTrade(@PathVariable Long userId, @PathVariable CoinService.CoinEnum coinCode, @RequestBody TradeDTO dto) {
        try {
            return Response.ok("id", tradeService.postTrade(userId, coinCode, dto));
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError();
        }
    }

    @DeleteMapping("/user/{userId}/coins/{coinCode}/trade")
    public Response deleteTrade(@PathVariable Long userId, @PathVariable CoinService.CoinEnum coinCode, @RequestParam Long id) {
        try {
            tradeService.deleteTrade(id);

            return Response.ok(true);
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError();
        }
    }

    @GetMapping("/user/{userId}/coins/{coinCode}/trades")
    public Response getTrades(@PathVariable Long userId, @PathVariable CoinService.CoinEnum coinCode, @RequestParam(required = false) Integer type, @RequestParam(required = false) Integer index) {
        try {
            index = index == null || index <= 0 ? 1 : index;

            return Response.ok(tradeService.getTrades(userId, coinCode, type, index));
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError();
        }
    }

    @PostMapping("/user/{userId}/coins/{coinCode}/trade-request")
    public Response postTradeRequest(@PathVariable Long userId, @PathVariable CoinService.CoinEnum coinCode, @RequestBody TradeRequestDTO dto) {
        try {
            return Response.ok("id", tradeService.postTradeRequest(userId, coinCode, dto));
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError();
        }
    }
}