package com.batm.rest;

import com.batm.dto.TransactionDetailsDTO;
import com.batm.dto.WalletDTO;
import com.batm.service.CoinService;
import com.batm.service.WalletService;
import com.batm.util.Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/wallet")
public class WalletController {

    @Value("${wallet.api.key}")
    private String apiKey;

    @Value("${wallet.api.secret}")
    private String apiSecret;

    @Value("${wallet.api.security.enabled}")
    private Boolean enabled;

    @Autowired
    private WalletService walletService;

    @GetMapping("/validate")
    public ResponseEntity<WalletDTO> getValidate(@RequestParam CoinService.CoinEnum coin, @RequestParam String address, @RequestParam long timestamp, @RequestParam String signature) {
        String signature2 = Util.sign("validate", coin.name(), timestamp, apiKey, apiSecret);

        if (enabled && !signature2.equals(signature)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        WalletDTO dto = new WalletDTO();
        dto.setValid(coin.getCoinType().validate(address));

        return ResponseEntity.ok(dto);
    }

    @GetMapping("/price")
    public ResponseEntity<WalletDTO> getPrice(@RequestParam CoinService.CoinEnum coin, @RequestParam long timestamp, @RequestParam String signature) {
        String signature2 = Util.sign("price", coin.name(), timestamp, apiKey, apiSecret);

        if (enabled && !signature2.equals(signature)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        WalletDTO dto = new WalletDTO();
        dto.setPrice(coin.getPrice());

        return ResponseEntity.ok(dto);
    }

    @GetMapping("/balance")
    public ResponseEntity<WalletDTO> getBalance(@RequestParam CoinService.CoinEnum coin, @RequestParam long timestamp, @RequestParam String signature) {
        String signature2 = Util.sign("balance", coin.name(), timestamp, apiKey, apiSecret);

        if (enabled && !signature2.equals(signature)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        WalletDTO dto = new WalletDTO();
        dto.setBalance(walletService.getBalance(coin));

        return ResponseEntity.ok(dto);
    }

    @GetMapping("/settings")
    public ResponseEntity<WalletDTO> getSettings(@RequestParam CoinService.CoinEnum coin, @RequestParam long timestamp, @RequestParam String signature) {
        String signature2 = Util.sign("settings", coin.name(), timestamp, apiKey, apiSecret);

        if (enabled && !signature2.equals(signature)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        WalletDTO dto = new WalletDTO();
        dto.setAddress(coin.getWalletAddress());
        dto.setTxFee(coin.getTxFee());
        dto.setTxTolerance(coin.getCoinEntity().getTolerance().stripTrailingZeros());
        dto.setScale(coin.getCoinEntity().getScale());

        return ResponseEntity.ok(dto);
    }

    @GetMapping("/generate-new-address")
    public ResponseEntity<WalletDTO> generateNewAddress(@RequestParam CoinService.CoinEnum coin, @RequestParam long timestamp, @RequestParam String signature) {
        String signature2 = Util.sign("generate-new-address", coin.name(), timestamp, apiKey, apiSecret);

        if (enabled && !signature2.equals(signature)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        WalletDTO dto = new WalletDTO();
        dto.setNewAddress(walletService.generateNewAddress(coin));

        return ResponseEntity.ok(dto);
    }

    @GetMapping("/transaction")
    public ResponseEntity<WalletDTO> getTransaction(@RequestParam CoinService.CoinEnum coin, @RequestParam String txId, @RequestParam String address, @RequestParam long timestamp, @RequestParam String signature) {
        String signature2 = Util.sign("transaction", coin.name(), timestamp, apiKey, apiSecret);

        if (enabled && !signature2.equals(signature)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        TransactionDetailsDTO transaction = coin.getTransaction(txId, address);

        WalletDTO dto = new WalletDTO();
        dto.setTxId(txId);
        dto.setFromAddress(transaction.getFromAddress());
        dto.setToAddress(transaction.getToAddress());
        dto.setAmount(transaction.getCryptoAmount().stripTrailingZeros());
        dto.setConfirmations(transaction.getConfirmations());

        return ResponseEntity.ok(dto);
    }

    @GetMapping("/received-addresses")
    public ResponseEntity<WalletDTO> getReceivedAddresses(@RequestParam CoinService.CoinEnum coin, @RequestParam Set<String> addresses, @RequestParam long timestamp, @RequestParam String signature) {
        String signature2 = Util.sign("received-addresses", coin.name(), timestamp, apiKey, apiSecret);

        if (enabled && !signature2.equals(signature)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        WalletDTO dto = new WalletDTO();
        dto.setReceivedAddresses(walletService.getReceivedAddresses(coin, addresses));

        return ResponseEntity.ok(dto);
    }

    @GetMapping("/send")
    public ResponseEntity<WalletDTO> sendCoins(@RequestParam CoinService.CoinEnum coin, @RequestParam String fromAddress, @RequestParam String toAddress, @RequestParam BigDecimal amount, @RequestParam long timestamp, @RequestParam String signature) {
        String signature2 = Util.sign("send", coin.name(), timestamp, apiKey, apiSecret);

        if (enabled && !signature2.equals(signature)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        WalletDTO dto = new WalletDTO();
        dto.setTxId(walletService.sendCoins(coin, fromAddress, toAddress, amount));

        return ResponseEntity.ok(dto);
    }
}