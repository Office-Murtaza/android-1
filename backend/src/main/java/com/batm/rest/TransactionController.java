package com.batm.rest;

import com.batm.dto.SubmitTransactionDTO;
import com.batm.model.Response;
import com.batm.model.TransactionType;
import com.batm.service.CoinService;
import com.batm.service.GethService;
import com.batm.service.TransactionService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private GethService geth;

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
                txId = transactionService.recall(userId, coin, dto);
            } else {
                txId = coin.submitTransaction(dto.getHex());
            }

            if (StringUtils.isNotBlank(txId)) {
                if (TransactionType.SEND_GIFT.getValue() == dto.getType()) {
                    transactionService.saveGift(userId, coin, txId, dto);
                }

                if (TransactionType.SEND_EXCHANGE.getValue() == dto.getType()) {
                    transactionService.exchange(userId, coin, txId, dto);
                }

                if (TransactionType.RESERVE.getValue() == dto.getType()) {
                    transactionService.reserve(userId, coin, txId, dto);
                }

                if (TransactionType.STAKE.getValue() == dto.getType()) {
                    transactionService.stake(userId, coin, txId, dto.getCryptoAmount());
                }

                if (TransactionType.UNSTAKE.getValue() == dto.getType()) {
                    transactionService.unstake(userId, coin, txId, dto.getCryptoAmount());
                }

                if (coin == CoinService.CoinEnum.ETH) {
                    geth.addPendingEthTransaction(txId, dto.getFromAddress(), dto.getToAddress(), dto.getCryptoAmount(), dto.getFee());
                }

                if (coin == CoinService.CoinEnum.CATM) {
                    geth.addPendingTokenTransaction(txId, dto.getFromAddress(), dto.getToAddress(), dto.getCryptoAmount(), dto.getFee());
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