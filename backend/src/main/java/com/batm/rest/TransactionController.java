package com.batm.rest;

import com.batm.dto.SubmitTransactionDTO;
import com.batm.dto.TradeDTO;
import com.batm.model.Response;
import com.batm.model.TransactionType;
import com.batm.service.CoinService;
import com.batm.service.TransactionService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    /**
     * Transaction History
     */
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

    /**
     * Transaction Details
     */
    @GetMapping("/user/{userId}/coins/{coinCode}/transaction/{txId}")
    public Response getTransaction(@PathVariable Long userId, @PathVariable CoinService.CoinEnum coinCode, @PathVariable String txId) {
        try {
            return Response.ok(transactionService.getTransactionDetails(userId, coinCode, txId));
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError();
        }
    }

    @GetMapping("/user/{userId}/coins/{coinCode}/transactions/utxo/{xpub}")
    public Response getUtxo(@PathVariable Long userId, @PathVariable CoinService.CoinEnum coinCode, @PathVariable String xpub) {
        try {
            if (coinCode == CoinService.CoinEnum.BTC || coinCode == CoinService.CoinEnum.BCH || coinCode == CoinService.CoinEnum.LTC) {
                return Response.ok(coinCode.getUTXO(xpub));
            } else {
                return Response.error(2, coinCode.name() + " not allowed");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError();
        }
    }

    @GetMapping("/user/{userId}/coins/{coinCode}/transactions/nonce")
    public Response getNonce(@PathVariable Long userId, @PathVariable CoinService.CoinEnum coinCode, @RequestParam String address) {
        try {
            if (coinCode == CoinService.CoinEnum.ETH || coinCode == CoinService.CoinEnum.CATM) {
                return Response.ok(coinCode.getNonce(address));
            } else {
                return Response.error(2, coinCode.name() + " not allowed");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError();
        }
    }

    @GetMapping("/user/{userId}/coins/{coinCode}/transactions/currentaccount")
    public Response getCurrentAccount(@PathVariable Long userId, @PathVariable CoinService.CoinEnum coinCode) {
        try {
            if (coinCode == CoinService.CoinEnum.BNB || coinCode == CoinService.CoinEnum.XRP) {
                return Response.ok(coinCode.getCurrentAccount(userId));
            } else {
                return Response.error(2, coinCode.name() + " not allowed");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError();
        }
    }

    @GetMapping("/user/{userId}/coins/{coinCode}/transactions/currentblock")
    public Response getCurrentBlock(@PathVariable Long userId, @PathVariable CoinService.CoinEnum coinCode) {
        try {
            if (coinCode == CoinService.CoinEnum.TRX) {
                return Response.ok(coinCode.getCurrentBlock());
            } else {
                return Response.error(2, coinCode.name() + " not allowed");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError();
        }
    }

    @GetMapping("/user/{userId}/coins/{coinCode}/transactions/limits")
    public Response getLimits(@PathVariable Long userId, @PathVariable CoinService.CoinEnum coinCode) {
        try {
            return Response.ok(transactionService.getUserTransactionLimits(userId));
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError();
        }
    }

    @PostMapping("/user/{userId}/coins/{coinCode}/transactions/presubmit")
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
            if (StringUtils.isBlank(dto.getHex())) {
                if (TransactionType.RECALL.getValue() == dto.getType()) {
                    String txId = transactionService.recall(userId, coinCode, dto);

                    return Response.ok("txId", txId);
                }
            } else {
                String txId = coinCode.submitTransaction(dto.getHex());

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

                    return Response.ok("txId", txId);
                }
            }

            return Response.error(2, coinCode.name() + " error transaction creation");
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError();
        }
    }

    @PostMapping("/user/{userId}/coins/{coinCode}/transactions/trade")
    public Response postTrade(@PathVariable Long userId, @PathVariable CoinService.CoinEnum coinCode, @RequestBody TradeDTO dto) {
        try {
            return Response.ok("id", transactionService.postTrade(userId, coinCode, dto));
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError();
        }
    }

    @DeleteMapping("/user/{userId}/coins/{coinCode}/transactions/trade")
    public Response deleteTrade(@PathVariable Long userId, @PathVariable CoinService.CoinEnum coinCode, @RequestParam Long id) {
        try {
            transactionService.deleteTrade(id);

            return Response.ok(true);
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError();
        }
    }

    @GetMapping("/user/{userId}/coins/{coinCode}/transactions/trade")
    public Response getTrades(@PathVariable Long userId, @PathVariable CoinService.CoinEnum coinCode, @RequestParam(required = false) Integer type, @RequestParam(required = false) Integer index) {
        try {
            index = index == null || index <= 0 ? 1 : index;

            return Response.ok(transactionService.getTrades(userId, coinCode, type, index));
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError();
        }
    }
}