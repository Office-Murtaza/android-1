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

    @PostMapping("/user/{userId}/coin/{coin}/trade")
    public Response createTrade(@PathVariable Long userId, @PathVariable CoinService.CoinEnum coin, @RequestBody TradeDTO dto) {
        try {
            return Response.ok("id", tradeService.createTrade(userId, coin, dto));
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError();
        }
    }

    @PutMapping("/user/{userId}/coin/{coin}/trade")
    public Response updateTrade(@PathVariable Long userId, @PathVariable CoinService.CoinEnum coin, @RequestBody TradeDTO dto) {
        try {
            return Response.ok(tradeService.updateTrade(dto));
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError();
        }
    }

    @DeleteMapping("/user/{userId}/coin/{coin}/trade")
    public Response deleteTrade(@PathVariable Long userId, @PathVariable CoinService.CoinEnum coin, @RequestParam Long id) {
        try {
            return Response.ok(tradeService.deleteTrade(id));
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError();
        }
    }

    @GetMapping("/user/{userId}/coin/{coin}/trade-history")
    public Response getTrades(@PathVariable Long userId, @PathVariable CoinService.CoinEnum coin, @RequestParam Integer tab, @RequestParam Integer index, @RequestParam Integer sort) {
        try {
            index = index == null || index <= 0 ? 1 : index;
            sort = sort == null ? 1 : sort;

            return Response.ok(tradeService.getTrades(userId, coin, tab, index, sort));
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError();
        }
    }

    @PostMapping("/user/{userId}/coin/{coin}/trade-request")
    public Response createTradeRequest(@PathVariable Long userId, @PathVariable CoinService.CoinEnum coin, @RequestBody TradeRequestDTO dto) {
        try {
            return Response.ok("id", tradeService.createTradeRequest(userId, coin, dto));
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError();
        }
    }

    @PutMapping("/user/{userId}/coin/{coin}/trade-request")
    public Response updateTradeRequest(@PathVariable Long userId, @PathVariable CoinService.CoinEnum coin, @RequestBody TradeRequestDTO dto) {
        try {
            return Response.ok(tradeService.updateTradeRequest(userId, dto));
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError();
        }
    }

    @GetMapping("/user/{userId}/coin/{coin}/trade-request-history")
    public Response getTradeRequestHistory(@PathVariable Long userId, @PathVariable CoinService.CoinEnum coin, @RequestParam Integer index) {
        try {
            index = index == null || index <= 0 ? 1 : index;

            return Response.ok(tradeService.getTradeRequests(userId, coin, index));
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError();
        }
    }
}