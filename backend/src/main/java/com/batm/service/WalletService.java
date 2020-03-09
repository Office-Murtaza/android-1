package com.batm.service;

import com.batm.dto.BlockchainTransactionsDTO;
import com.batm.dto.SignDTO;
import com.batm.dto.TransactionDTO;
import com.batm.entity.Coin;
import com.batm.entity.CoinPath;
import com.batm.entity.TransactionRecordWallet;
import com.batm.model.TransactionStatus;
import com.batm.model.TransactionType;
import com.batm.repository.CoinPathRep;
import com.batm.repository.TransactionRecordWalletRep;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import wallet.core.jni.*;
import javax.annotation.PostConstruct;
import java.math.BigDecimal;

@Slf4j
@Getter
@Service
public class WalletService {

//    static {
//        System.loadLibrary("TrustWalletCore");
//    }

    @Value("${wallet.seed}")
    private String seed;

    @Autowired
    private CoinPathRep coinPathRep;

    @Autowired
    private TransactionRecordWalletRep transactionRecordWalletRep;

    private HDWallet wallet = null;

    private String addressBTC = null;
    private String addressBCH = null;
    private String addressETH = null;
    private String addressLTC = null;
    private String addressBNB = null;
    private String addressXRP = null;
    private String addressTRX = null;

    private PrivateKey privateKeyBTC = null;
    private PrivateKey privateKeyETH = null;
    private PrivateKey privateKeyXRP = null;
    private PrivateKey privateKeyTRX = null;
    private PrivateKey privateKeyBNB = null;

    @PostConstruct
    public void init() {
//        wallet = new HDWallet(seed, "");

//        privateKeyBTC = wallet.getKeyForCoin(CoinType.BITCOIN);
//        PublicKey publicKeyBTC = wallet.getPublicKeyFromExtended(getXPUB(CoinType.BITCOIN), getPath(CoinType.BITCOIN));
//        addressBTC = new BitcoinAddress(publicKeyBTC, CoinType.BITCOIN.p2pkhPrefix()).description();
//
//        PrivateKey privateKeyBCH = wallet.getKeyForCoin(CoinType.BITCOINCASH);
//        addressBCH = CoinType.BITCOINCASH.deriveAddress(privateKeyBCH);
//
//        privateKeyETH = wallet.getKeyForCoin(CoinType.ETHEREUM);
//        addressETH = CoinType.ETHEREUM.deriveAddress(privateKeyETH);
//
//        PrivateKey privateKeyLTC = wallet.getKeyForCoin(CoinType.LITECOIN);
//        addressLTC = CoinType.LITECOIN.deriveAddress(privateKeyLTC);
//
//        privateKeyBNB = wallet.getKeyForCoin(CoinType.BINANCE);
//        addressBNB = CoinType.BINANCE.deriveAddress(privateKeyBNB);
//
//        privateKeyXRP = wallet.getKeyForCoin(CoinType.XRP);
//        addressXRP = CoinType.XRP.deriveAddress(privateKeyXRP);
//
//        privateKeyTRX = wallet.getKeyForCoin(CoinType.TRON);
//        addressTRX = CoinType.TRON.deriveAddress(privateKeyTRX);
    }

    public String getPath(CoinType coinType) {
        if (coinType == CoinType.BITCOIN) {
            return "m/44'/0'/0'/0/0";
        } else {
            return coinType.derivationPath();
        }
    }

    public String getXPUB(CoinType coinType) {
        if (coinType == CoinType.BITCOIN || coinType == CoinType.XRP) {
            return wallet.getExtendedPublicKey(Purpose.BIP44, coinType, HDVersion.XPUB);
        } else {
            return wallet.getExtendedPublicKey(coinType.purpose(), coinType, coinType.xpubVersion());
        }
    }

    public String generateNewPath(String path, Integer index) {
        return path.substring(0, path.length() - 1) + index;
    }

    public String generateNewAddress(CoinType coinType, String newPath) {
        if (coinType == CoinType.BITCOIN) {
            PublicKey publicKey = wallet.getPublicKeyFromExtended(getXPUB(coinType), newPath);

            return new BitcoinAddress(publicKey, CoinType.BITCOIN.p2pkhPrefix()).description();
        } else if (coinType == CoinType.BITCOINCASH || coinType == CoinType.LITECOIN) {
            PublicKey publicKey = wallet.getPublicKeyFromExtended(getXPUB(coinType), newPath);

            return coinType.deriveAddressFromPublicKey(publicKey);
        } else if (coinType == CoinType.XRP) {
            PublicKey publicKey = wallet.getPublicKeyFromExtended(getXPUB(coinType), newPath);

            return new RippleAddress(publicKey).description();
        } else if (coinType == CoinType.ETHEREUM) {
            PrivateKey privateKey = wallet.getKey(newPath);

            return new EthereumAddress(privateKey.getPublicKeySecp256k1(false)).description();
        } else if (coinType == CoinType.TRON) {
            PrivateKey privateKey = wallet.getKey(newPath);

            return new TronAddress(privateKey.getPublicKeySecp256k1(false)).description();
        } else if (coinType == CoinType.BINANCE) {
            PrivateKey privateKey = wallet.getKey(newPath);

            return new CosmosAddress(HRP.BINANCE, privateKey.getPublicKeySecp256k1(true)).description();
        }

        return null;
    }

    public String getCryptoAddress(CoinService.CoinEnum coinCode) {
        try {
            CoinType coinType = coinCode.getCoinType();
            Coin coinEntity = coinCode.getCoinEntity();
            Integer index = coinPathRep.countCoinPathByCoin(coinEntity);

            String path = getPath(coinType);
            String newPath = generateNewPath(path, index + 1);
            String address = generateNewAddress(coinType, newPath);

            CoinPath coinPath = new CoinPath();
            coinPath.setPath(newPath);
            coinPath.setAddress(address);
            coinPath.setCoin(coinEntity);
            coinPathRep.save(coinPath);

            return address;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public BigDecimal getCryptoBalance(CoinService.CoinEnum coinCode) {
        try {
            String walletAddress = coinCode.getWalletAddress();
            BigDecimal balance = coinCode.getBalance(walletAddress);
            BlockchainTransactionsDTO blockchainTransactionsDTO = coinCode.getBlockchainTransactions(walletAddress);

            BigDecimal pendingSum = blockchainTransactionsDTO.getMap().values().stream()
                    .filter(e -> e.getType() == TransactionType.WITHDRAW && e.getStatus() == TransactionStatus.PENDING)
                    .map(TransactionDTO::getCryptoAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            return balance.subtract(pendingSum);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public String sendCoins(CoinService.CoinEnum coinCode, String toAddress, BigDecimal amount) {
        try {
            SignDTO signDTO = coinCode.buildSignDTOFromMainWallet();
            String hex = coinCode.sign(toAddress, amount, signDTO);
            String txId = coinCode.submitTransaction(hex);

            TransactionRecordWallet wallet = new TransactionRecordWallet();
            wallet.setCoin(coinCode.getCoinEntity());
            wallet.setAmount(amount);
            wallet.setType(TransactionType.SELL.getValue());
            wallet.setTxId(txId);
            wallet.setStatus(coinCode.getTransactionStatus(txId).getValue());

            transactionRecordWalletRep.save(wallet);

            return txId;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

//    public void transferToMainAddress(String fromAddress, BigDecimal amount, CoinService.CoinEnum coin) {
//        coin.getCoinDTOToServerWallet
//        String txId = coin.sign(dto)
//        log to DB
//        return txId;
//    }
}