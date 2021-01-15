package com.belco.server.rest;

import com.belco.server.dto.SubmitTransactionDTO;
import com.belco.server.model.Response;
import com.belco.server.model.TransactionType;
import com.belco.server.service.CoinService;
import com.belco.server.service.TransactionService;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
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

    @GetMapping("/user/{userId}/coin/{coin}/transaction-details")
    public Response getTransactionDetails(@PathVariable Long userId, @PathVariable CoinService.CoinEnum coin, @RequestParam String txId) {
        try {
            return Response.ok(transactionService.getTransactionDetails(userId, coin, txId));
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError();
        }
    }

    @PostMapping("/user/{userId}/coin/{coin}/pre-submit")
    public Response preSubmit(@PathVariable Long userId, @PathVariable CoinService.CoinEnum coin, @RequestBody SubmitTransactionDTO dto) {
        try {
            return Response.ok(transactionService.preSubmit(userId, coin, dto));
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError();
        }
    }

    @PostMapping("/user/{userId}/coin/{coin}/submit")
    public Response submit(@PathVariable Long userId, @PathVariable CoinService.CoinEnum coin, @RequestBody SubmitTransactionDTO dto) {
        try {
            String txId;

            if (TransactionType.RECALL.getValue() == dto.getType()) {
                return transactionService.recall(userId, coin, dto);
            } else {
                txId = coin.submitTransaction(dto);
            }

            if (StringUtils.isNotBlank(txId)) {
                if (TransactionType.SEND_TRANSFER.getValue() == dto.getType()) {
                    transactionService.persistTransfer(userId, coin, txId, dto);
                }

                if (TransactionType.SEND_SWAP.getValue() == dto.getType()) {
                    transactionService.swap(userId, coin, txId, dto);
                }

                if (TransactionType.RESERVE.getValue() == dto.getType()) {
                    transactionService.reserve(userId, coin, txId, dto);
                }

                if (TransactionType.CREATE_STAKE.getValue() == dto.getType()) {
                    transactionService.createStake(userId, coin, txId, dto.getCryptoAmount());
                }

                if (TransactionType.CANCEL_STAKE.getValue() == dto.getType()) {
                    transactionService.cancelStake(userId, coin, txId, dto.getCryptoAmount());
                }

                if (TransactionType.WITHDRAW_STAKE.getValue() == dto.getType()) {
                    transactionService.withdrawStake(userId, coin, txId, dto.getCryptoAmount());
                }

                return Response.ok("txId", txId);
            }

            return Response.defaultError(coin.name() + " submit transaction error");
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError();
        }
    }

    @GetMapping("/user/{userId}/coin/{coin}/stake-details")
    public Response getStakeDetails(@PathVariable Long userId, @PathVariable CoinService.CoinEnum coin) {
        try {
            return Response.ok(transactionService.getStakeDetails(userId, coin));
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError();
        }
    }
}