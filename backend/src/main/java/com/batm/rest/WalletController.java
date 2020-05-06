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

    @Autowired
    private WalletService walletService;

    @GetMapping("/validate")
    public ResponseEntity<WalletDTO> getValidate(@RequestParam CoinService.CoinEnum coinCode, @RequestParam String address, @RequestParam long timestamp, @RequestParam String signature) {
        String signature2 = Util.sign("validate", coinCode.name(), timestamp, apiKey, apiSecret);
        System.out.println(" ---- /validate coinCode: " + coinCode.name() + ", address: " + address + ", timestamp: " + timestamp + ", signature: " + signature + ", signature2: " + signature2);

        if (!signature2.equals(signature)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        WalletDTO dto = new WalletDTO();
        dto.setValid(coinCode.getCoinType().validate(address));

        //System.out.println(" ==== result: " + dto.getValid());
        System.out.println("\n");

        return ResponseEntity.ok(dto);
    }

    @GetMapping("/price")
    public ResponseEntity<WalletDTO> getPrice(@RequestParam CoinService.CoinEnum coinCode, @RequestParam long timestamp, @RequestParam String signature) {
        String signature2 = Util.sign("price", coinCode.name(), timestamp, apiKey, apiSecret);
        System.out.println(" ---- /price coinCode: " + coinCode.name() + ", timestamp: " + timestamp + ", signature: " + signature + ", signature2: " + signature2);

        if (!signature2.equals(signature)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        WalletDTO dto = new WalletDTO();
        dto.setPrice(coinCode.getPrice());

        //System.out.println(" ==== result: " + dto.getPrice());
        System.out.println("\n");

        return ResponseEntity.ok(dto);
    }

    @GetMapping("/balance")
    public ResponseEntity<WalletDTO> getBalance(@RequestParam CoinService.CoinEnum coinCode, @RequestParam long timestamp, @RequestParam String signature) {
        String signature2 = Util.sign("balance", coinCode.name(), timestamp, apiKey, apiSecret);
        System.out.println(" ---- /balance coinCode: " + coinCode.name() + ", timestamp: " + timestamp + ", signature: " + signature + ", signature2: " + signature2);

        if (!signature2.equals(signature)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        WalletDTO dto = new WalletDTO();
        dto.setBalance(walletService.getBalance(coinCode));

        //System.out.println(" ==== result: " + dto.getBalance());
        System.out.println("\n");

        return ResponseEntity.ok(dto);
    }

    @GetMapping("/settings")
    public ResponseEntity<WalletDTO> getSettings(@RequestParam CoinService.CoinEnum coinCode, @RequestParam long timestamp, @RequestParam String signature) {
        String signature2 = Util.sign("settings", coinCode.name(), timestamp, apiKey, apiSecret);
        System.out.println(" ---- /settings coinCode: " + coinCode.name() + ", timestamp: " + timestamp + ", signature: " + signature + ", signature2: " + signature2);

        if (!signature2.equals(signature)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        WalletDTO dto = new WalletDTO();
        dto.setAddress(coinCode.getWalletAddress());
        dto.setTxFee(coinCode.getCoinSettings().getTxFee());
        dto.setTxTolerance(coinCode.getCoinEntity().getTolerance().stripTrailingZeros());
        dto.setScale(coinCode.getCoinEntity().getScale());

        //System.out.println(" ==== result: " + dto.getAddress());
        System.out.println("\n");

        return ResponseEntity.ok(dto);
    }

    @GetMapping("/generate-new-address")
    public ResponseEntity<WalletDTO> generateNewAddress(@RequestParam CoinService.CoinEnum coinCode, @RequestParam long timestamp, @RequestParam String signature) {
        String signature2 = Util.sign("generate-new-address", coinCode.name(), timestamp, apiKey, apiSecret);
        System.out.println(" ---- /generate-new-address coinCode: " + coinCode.name() + ", timestamp: " + timestamp + ", signature: " + signature + ", signature2: " + signature2);

        if (!signature2.equals(signature)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        WalletDTO dto = new WalletDTO();
        dto.setNewAddress(walletService.generateNewAddress(coinCode));

        //System.out.println(" ==== result: " + dto.getNewAddress());
        System.out.println("\n");

        return ResponseEntity.ok(dto);
    }

    @GetMapping("/transaction")
    public ResponseEntity<WalletDTO> getTransaction(@RequestParam CoinService.CoinEnum coinCode, @RequestParam String txId, @RequestParam long timestamp, @RequestParam String signature) {
        String signature2 = Util.sign("transaction", coinCode.name(), timestamp, apiKey, apiSecret);
        System.out.println(" ---- /transaction coinCode: " + coinCode.name() + ", txId: " + txId + ", timestamp: " + timestamp + ", signature: " + signature + ", signature2: " + signature2);

        if (!signature2.equals(signature)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        TransactionDetailsDTO transaction = coinCode.getTransaction(txId, null);

        WalletDTO dto = new WalletDTO();
        dto.setTxId(txId);
        dto.setFromAddress(transaction.getFromAddress());
        dto.setToAddress(transaction.getToAddress());
        dto.setAmount(transaction.getCryptoAmount().stripTrailingZeros());
        dto.setConfirmations(transaction.getConfirmations());

        //System.out.println(" ==== result: " + dto.getTxId());
        System.out.println("\n");

        return ResponseEntity.ok(dto);
    }

    @GetMapping("/received-addresses")
    public ResponseEntity<WalletDTO> getReceivedAddresses(@RequestParam CoinService.CoinEnum coinCode, @RequestParam Set<String> addresses, @RequestParam long timestamp, @RequestParam String signature) {
        String signature2 = Util.sign("received-addresses", coinCode.name(), timestamp, apiKey, apiSecret);
        System.out.println(" ---- /received-addresses coinCode: " + coinCode.name() + ", addresses: " + addresses + ", timestamp: " + timestamp + ", signature: " + signature + ", signature2: " + signature2);

        if (!signature2.equals(signature)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        WalletDTO dto = new WalletDTO();
        dto.setReceivedAddresses(walletService.getReceivedAddresses(coinCode, addresses));

        //System.out.println(" ==== result: " + dto.getReceivedAddresses());
        System.out.println("\n");

        return ResponseEntity.ok(dto);
    }

    @GetMapping("/send")
    public ResponseEntity<WalletDTO> sendCoins(@RequestParam CoinService.CoinEnum coinCode, @RequestParam String fromAddress, @RequestParam String toAddress, @RequestParam BigDecimal amount, @RequestParam long timestamp, @RequestParam String signature) {
        String signature2 = Util.sign("send", coinCode.name(), timestamp, apiKey, apiSecret);
        System.out.println(" ---- /send coinCode: " + coinCode.name() + ", fromAddress: " + fromAddress + ", toAddress: " + toAddress + ", amount: " + amount + ", timestamp: " + timestamp + ", signature: " + signature + ", signature2: " + signature2);

        if (!signature2.equals(signature)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        WalletDTO dto = new WalletDTO();
        dto.setTxId(walletService.sendCoins(coinCode, fromAddress, toAddress, amount));

        //System.out.println(" ==== result: " + dto.getTxId());
        System.out.println("\n");

        return ResponseEntity.ok(dto);
    }
}