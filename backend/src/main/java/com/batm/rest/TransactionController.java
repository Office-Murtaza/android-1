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

    @GetMapping("/user/{userId}/coins/{coinCode}/transactions")
    public Response getTransactions(@PathVariable Long userId, @PathVariable CoinService.CoinEnum coinCode, @RequestParam(required = false) Integer index) {
        try {
            index = index == null || index <= 0 ? 1 : index;

            return Response.ok(transactionService.getTransactionHistory(userId, coinCode, index));
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError();
        }
    }

    @GetMapping("/user/{userId}/coins/{coinCode}/transaction")
    public Response getTransaction(@PathVariable Long userId, @PathVariable CoinService.CoinEnum coinCode, @RequestParam String txId) {
        try {
            return Response.ok(transactionService.getTransactionDetails(userId, coinCode, txId));
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError();
        }
    }

    @PostMapping("/user/{userId}/coins/{coinCode}/transactions/pre-submit")
    public Response preSubmit(@PathVariable Long userId, @PathVariable CoinService.CoinEnum coinCode, @RequestBody SubmitTransactionDTO dto) {
        try {
            return Response.ok(transactionService.preSubmit(userId, coinCode, dto));
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError();
        }
    }

    @PostMapping("/user/{userId}/coins/{coinCode}/transactions/submit")
    public Response submit(@PathVariable Long userId, @PathVariable CoinService.CoinEnum coinCode, @RequestBody SubmitTransactionDTO dto) {
        try {
            String txId;

            if (TransactionType.RECALL.getValue() == dto.getType()) {
                txId = transactionService.recall(userId, coinCode, dto);
            } else {
                txId = coinCode.submitTransaction(dto.getHex());
            }

            if (StringUtils.isNotBlank(txId)) {
                if (TransactionType.SEND_GIFT.getValue() == dto.getType()) {
                    transactionService.saveGift(userId, coinCode, txId, dto);
                }

                if (TransactionType.SEND_EXCHANGE.getValue() == dto.getType()) {
                    transactionService.exchange(userId, coinCode, txId, dto);
                }

                if (TransactionType.RESERVE.getValue() == dto.getType()) {
                    transactionService.reserve(userId, coinCode, txId, dto);
                }

                if (TransactionType.STAKE.getValue() == dto.getType()) {
                    transactionService.stake(userId, coinCode, txId, dto.getCryptoAmount());
                }

                if (TransactionType.UNSTAKE.getValue() == dto.getType()) {
                    transactionService.unstake(userId, coinCode, txId, dto.getCryptoAmount());
                }

                if (coinCode == CoinService.CoinEnum.ETH) {
                    geth.addPendingEthTransaction(txId.toLowerCase(), dto.getFromAddress(), dto.getToAddress(), dto.getCryptoAmount(), dto.getFee());
                }

                if (coinCode == CoinService.CoinEnum.CATM) {
                    geth.addPendingTokenTransaction(txId.toLowerCase(), dto.getFromAddress(), dto.getToAddress(), dto.getCryptoAmount(), dto.getFee());
                }

                return Response.ok("txId", txId);
            }

            return Response.error(2, coinCode.name() + " submit transaction error");
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError();
        }
    }

    @GetMapping("/user/{userId}/coins/{coinCode}/transactions/stake-details")
    public Response getStakeDetails(@PathVariable Long userId, @PathVariable CoinService.CoinEnum coinCode) {
        try {
            return Response.ok(transactionService.getStakeDetails(userId, coinCode));
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError();
        }
    }
}