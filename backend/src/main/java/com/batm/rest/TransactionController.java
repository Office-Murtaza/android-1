package com.batm.rest;

import com.batm.dto.SendTransactionDTO;
import com.batm.entity.Response;
import com.batm.service.CoinService;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class TransactionController {

    @Autowired
    private CoinService coinService;

    @GetMapping("/user/{userId}/coins/{coinId}/transactions")
    public Response getTransactions(@PathVariable Long userId, @PathVariable CoinService.CoinEnum coinId, @RequestParam(required = false) Integer index) {
        try {
            index = index == null || index <= 0 ? 1 : index;

            return Response.ok(coinService.getTransactions(userId, coinId, index));
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError();
        }
    }

    @GetMapping("/user/{userId}/coins/{coinId}/transactions/utxo/{xpub}")
    public Response getUtxo(@PathVariable String userId, @PathVariable CoinService.CoinEnum coinId, @PathVariable String xpub) {
        try {
            if (coinId == CoinService.CoinEnum.BTC || coinId == CoinService.CoinEnum.ETH || coinId == CoinService.CoinEnum.BCH || coinId == CoinService.CoinEnum.LTC) {
                return Response.ok(coinId.getUTXO(xpub));
            } else {
                return Response.error(2, coinId.name() + " not allowed");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError();
        }
    }

    @GetMapping("/user/{userId}/coins/{coinId}/transactions/nonce/{address}")
    public Response getNonce(@PathVariable String userId, @PathVariable CoinService.CoinEnum coinId, @PathVariable String address) {
        try {
            if (coinId == CoinService.CoinEnum.ETH) {
                return Response.ok(coinId.getNonce(address));
            } else {
                return Response.error(2, coinId.name() + " not allowed");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError();
        }
    }

    @GetMapping("/user/{userId}/coins/{coinId}/transactions/currentaccount/{address}")
    public Response getCurrentAccount(@PathVariable String userId, @PathVariable CoinService.CoinEnum coinId, @PathVariable String address) {
        try {
            if (coinId == CoinService.CoinEnum.BNB) {
                return Response.ok(coinId.getCurrentAccount(address));
            } else {
                return Response.error(2, coinId.name() + " not allowed");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError();
        }
    }

    @GetMapping("/user/{userId}/coins/{coinId}/transactions/currentblock")
    public Response getCurrentBlock(@PathVariable String userId, @PathVariable CoinService.CoinEnum coinId) {
        try {
            if (coinId == CoinService.CoinEnum.TRX) {
                return Response.ok(coinId.getCurrentBlock());
            } else {
                return Response.error(2, coinId.name() + " not allowed");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError();
        }
    }

    @GetMapping("/user/{userId}/coins/{coinId}/sendtx/{hex}")
    public Response sendTx(@PathVariable Long userId, @PathVariable CoinService.CoinEnum coinId, @PathVariable String hex) {
        try {
            JSONObject res = new JSONObject();
            SendTransactionDTO dto = coinId.sendTx(hex);

            if (dto.getSuccess()) {
                res.put("txId", dto.getTxId());
                return Response.ok(res);
            } else {
                return Response.sendTxError(dto.getErrorMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError();
        }
    }
}