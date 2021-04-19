package com.belco.server.rest;

import com.belco.server.dto.TransactionDetailsDTO;
import com.belco.server.dto.WalletDTO;
import com.belco.server.service.CoinService;
import com.belco.server.service.WalletService;
import com.belco.server.util.Util;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/wallet")
public class WalletController {

    private final WalletService walletService;

    @Value("${wallet.api.security.enabled}")
    private Boolean enabled;
    @Value("${wallet.api.security.key}")
    private String apiKey;
    @Value("${wallet.api.security.secret}")
    private String apiSecret;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @GetMapping("/coin/{coin}/validate")
    public ResponseEntity<WalletDTO> validate(@PathVariable CoinService.CoinEnum coin, @RequestParam String address, @RequestParam long timestamp, @RequestParam String signature) {
        String signature2 = Util.sign("validate", coin.name(), timestamp, apiKey, apiSecret);

        if (enabled && !signature2.equals(signature)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        WalletDTO dto = new WalletDTO();
        dto.setValid(coin.getCoinType().validate(address));

        return ResponseEntity.ok(dto);
    }

    @GetMapping("/coin/{coin}/price")
    public ResponseEntity<WalletDTO> getPrice(@PathVariable CoinService.CoinEnum coin, @RequestParam long timestamp, @RequestParam String signature) {
        String signature2 = Util.sign("price", coin.name(), timestamp, apiKey, apiSecret);

        if (enabled && !signature2.equals(signature)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        WalletDTO dto = new WalletDTO();
        dto.setPrice(coin.getPrice());

        return ResponseEntity.ok(dto);
    }

    @GetMapping("/coin/{coin}/balance")
    public ResponseEntity<WalletDTO> getBalance(@PathVariable CoinService.CoinEnum coin, @RequestParam long timestamp, @RequestParam String signature) {
        String signature2 = Util.sign("balance", coin.name(), timestamp, apiKey, apiSecret);

        if (enabled && !signature2.equals(signature)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        WalletDTO dto = new WalletDTO();
        dto.setBalance(walletService.getBalance(coin, coin.getWalletAddress()));

        return ResponseEntity.ok(dto);
    }

    @GetMapping("/coin/{coin}/details")
    public ResponseEntity<WalletDTO> getSettings(@PathVariable CoinService.CoinEnum coin, @RequestParam long timestamp, @RequestParam String signature) {
        String signature2 = Util.sign("details", coin.name(), timestamp, apiKey, apiSecret);

        if (enabled && !signature2.equals(signature)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        WalletDTO dto = new WalletDTO();
        dto.setWalletAddress(coin.getWalletAddress());
        dto.setTxFee(coin.getTxFee());
        dto.setTolerance(coin.getCoinEntity().getTolerance().stripTrailingZeros());
        dto.setScale(coin.getCoinEntity().getScale());

        if (coin == CoinService.CoinEnum.CATM || coin == CoinService.CoinEnum.USDC) {
            dto.setConvertedTxFee(walletService.convertToFee(coin));
        }

        return ResponseEntity.ok(dto);
    }

    @GetMapping("/coin/{coin}/new-address")
    public ResponseEntity<WalletDTO> getNewAddress(@PathVariable CoinService.CoinEnum coin, @RequestParam long timestamp, @RequestParam String signature) {
        String signature2 = Util.sign("receiving-address", coin.name(), timestamp, apiKey, apiSecret);

        if (enabled && !signature2.equals(signature)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        WalletDTO dto = new WalletDTO();
        dto.setReceivingAddress(walletService.getReceivingAddress(coin));

        return ResponseEntity.ok(dto);
    }

    @GetMapping("/coin/{coin}/transaction")
    public ResponseEntity<TransactionDetailsDTO> getTransaction(@PathVariable CoinService.CoinEnum coin, @RequestParam String txId, @RequestParam String address, @RequestParam long timestamp, @RequestParam String signature) {
        String signature2 = Util.sign("transaction", coin.name(), timestamp, apiKey, apiSecret);

        if (enabled && !signature2.equals(signature)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(coin.getTransactionDetails(txId, address));
    }

    @GetMapping("/coin/{coin}/receiving-addresses-txs")
    public ResponseEntity<Map<String, List<String>>> getReceivingAddressesTxs(@PathVariable CoinService.CoinEnum coin, @RequestParam List<String> addresses, @RequestParam long timestamp, @RequestParam String signature) {
        String signature2 = Util.sign("receiving-addresses-txs", coin.name(), timestamp, apiKey, apiSecret);

        if (enabled && !signature2.equals(signature)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(walletService.getReceivingAddressesTxs(coin, addresses));
    }

    @GetMapping("/coin/{coin}/transfer")
    public ResponseEntity<TransactionDetailsDTO> transfer(@PathVariable CoinService.CoinEnum coin, @RequestParam String fromAddress, @RequestParam String toAddress, @RequestParam BigDecimal amount, @RequestParam long timestamp, @RequestParam String signature) {
        String signature2 = Util.sign("transfer", coin.name(), timestamp, apiKey, apiSecret);

        if (enabled && !signature2.equals(signature)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        TransactionDetailsDTO dto = new TransactionDetailsDTO();
        dto.setTxId(walletService.transfer(coin, fromAddress, toAddress, amount));

        return ResponseEntity.ok(dto);
    }
}