package com.belco.server.rest;

import com.belco.server.dto.TransactionDTO;
import com.belco.server.dto.TransactionDetailsDTO;
import com.belco.server.model.Response;
import com.belco.server.service.CoinService;
import com.belco.server.service.TransactionService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping("/user/{userId}/coin/{coin}/pre-submit")
    public Response preSubmit(@PathVariable Long userId, @PathVariable CoinService.CoinEnum coin, @RequestBody TransactionDTO dto) {
        try {
            return Response.ok(transactionService.preSubmit(userId, coin, dto));
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError();
        }
    }

    @PostMapping("/user/{userId}/coin/{coin}/submit")
    public Response submit(@PathVariable Long userId, @PathVariable CoinService.CoinEnum coin, @RequestBody TransactionDTO dto) {
        try {
            TransactionDetailsDTO tx = transactionService.submit(userId, coin, dto);

            if (tx != null) {
                return Response.ok(tx);
            } else {
                return Response.validationError(coin.name() + " submit transaction error");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError();
        }
    }

    @GetMapping("/user/{userId}/coin/{coin}/transaction-history")
    public Response getTransactionHistory(@PathVariable Long userId, @PathVariable CoinService.CoinEnum coin, @RequestParam(required = false) Integer index) {
        try {
            index = index == null || index <= 0 ? 1 : index;

            return Response.ok(transactionService.getTransactionHistory(userId, coin, index));
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError();
        }
    }

    @GetMapping("/user/{userId}/coin/{coin}/staking-details")
    public Response getStakingDetails(@PathVariable Long userId, @PathVariable CoinService.CoinEnum coin) {
        try {
            return Response.ok(transactionService.getStakingDetails(userId, coin));
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError();
        }
    }
}