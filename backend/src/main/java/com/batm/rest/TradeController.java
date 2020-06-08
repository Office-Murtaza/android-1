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
    public Response createTrade(@PathVariable Long userId, @PathVariable CoinService.CoinEnum coinCode, @RequestBody TradeDTO dto) {
        try {
            return Response.ok("id", tradeService.createTrade(userId, coinCode, dto));
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError();
        }
    }

    @PutMapping("/user/{userId}/coins/{coinCode}/trade")
    public Response updateTrade(@PathVariable Long userId, @PathVariable CoinService.CoinEnum coinCode, @RequestBody TradeDTO dto) {
        try {
            return Response.ok(tradeService.updateTrade(dto));
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError();
        }
    }

    @DeleteMapping("/user/{userId}/coins/{coinCode}/trade")
    public Response deleteTrade(@PathVariable Long userId, @PathVariable CoinService.CoinEnum coinCode, @RequestParam Long id) {
        try {
            return Response.ok(tradeService.deleteTrade(id));
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError();
        }
    }

    @GetMapping("/user/{userId}/coins/{coinCode}/trades")
    public Response getTrades(@PathVariable Long userId, @PathVariable CoinService.CoinEnum coinCode, @RequestParam(required = false) Integer index, @RequestParam(required = false) Integer sort) {
        try {
            index = index == null || index <= 0 ? 1 : index;
            sort = sort == null ? 1 : sort;

            return Response.ok(tradeService.getTrades(userId, coinCode, index, sort));
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError();
        }
    }

    @PostMapping("/user/{userId}/coins/{coinCode}/trade-request")
    public Response createTradeRequest(@PathVariable Long userId, @PathVariable CoinService.CoinEnum coinCode, @RequestBody TradeRequestDTO dto) {
        try {
            return Response.ok("id", tradeService.createTradeRequest(userId, coinCode, dto));
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError();
        }
    }

    @PutMapping("/user/{userId}/coins/{coinCode}/trade-request")
    public Response updateTradeRequest(@PathVariable Long userId, @PathVariable CoinService.CoinEnum coinCode, @RequestBody TradeRequestDTO dto) {
        try {
            return Response.ok(tradeService.updateTradeRequest(userId, dto));
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError();
        }
    }

    @GetMapping("/user/{userId}/coins/{coinCode}/trade-requests")
    public Response getTradeRequests(@PathVariable Long userId, @PathVariable CoinService.CoinEnum coinCode, @RequestParam(required = false) Integer type, @RequestParam(required = false) Integer index, @RequestParam(required = false) Integer sort) {
        try {
            index = index == null || index <= 0 ? 1 : index;
            sort = sort == null ? 1 : sort;

            return Response.ok(tradeService.getTradeRequests(userId, coinCode, type, index, sort));
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError();
        }
    }
}