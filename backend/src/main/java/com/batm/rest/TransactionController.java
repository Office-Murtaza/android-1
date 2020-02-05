package com.batm.rest;

import com.batm.dto.SubmitTransactionDTO;
import com.batm.model.Response;
import com.batm.service.CoinService;
import com.batm.service.TransactionService;
import net.sf.json.JSONObject;
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
    public Response getNonce(@PathVariable Long userId, @PathVariable CoinService.CoinEnum coinCode) {
        try {
            if (coinCode == CoinService.CoinEnum.ETH) {
                return Response.ok(coinCode.getNonce(userId));
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
    public Response preSubmit(@PathVariable Long userId, @PathVariable CoinService.CoinEnum coinCode, @RequestBody SubmitTransactionDTO transaction) {
        try {
            return Response.ok(transactionService.preSubmit(userId, coinCode, transaction));
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError();
        }
    }

    @PostMapping("/user/{userId}/coins/{coinCode}/transactions/submit")
    public Response submit(@PathVariable Long userId, @PathVariable CoinService.CoinEnum coinCode, @RequestBody SubmitTransactionDTO transaction) {
        try {
            String txId = coinCode.submitTransaction(userId, transaction);

            if (StringUtils.isNotBlank(txId)) {
                JSONObject res = new JSONObject();
                res.put("txId", txId);

                return Response.ok(res);
            } else {
                return Response.error(2, coinCode.name() + " error transaction creation");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError();
        }
    }
}