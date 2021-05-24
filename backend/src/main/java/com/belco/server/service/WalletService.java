package com.belco.server.service;

import com.belco.server.dto.CoinDTO;
import com.belco.server.dto.TransactionDTO;
import com.belco.server.dto.TransactionDetailsDTO;
import com.belco.server.dto.WalletDetailsDTO;
import com.belco.server.entity.Coin;
import com.belco.server.entity.CoinPath;
import com.belco.server.entity.Wallet;
import com.belco.server.model.TransactionStatus;
import com.belco.server.model.TransactionType;
import com.belco.server.repository.CoinPathRep;
import com.belco.server.repository.WalletRep;
import com.belco.server.util.Constant;
import com.belco.server.util.Util;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import wallet.core.jni.*;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Service
public class WalletService {

    static {
        System.loadLibrary("TrustWalletCore");
    }

    private final WalletRep walletRep;
    private final CoinPathRep coinPathRep;
    private final GethService gethService;
    private final MongoTemplate mongo;

    private final Map<Long, WalletDetailsDTO> wallets = new ConcurrentHashMap<>();
    private final Set<String> addresses = new HashSet<>();

    @Value("${wallet.seed.secret}")
    private String secret;

    public WalletService(WalletRep walletRep, CoinPathRep coinPathRep, @Lazy GethService gethService, MongoTemplate mongo) {
        this.walletRep = walletRep;
        this.coinPathRep = coinPathRep;
        this.gethService = gethService;
        this.mongo = mongo;
    }

    @PostConstruct
    public void init() {
        walletRep.findAll().stream().forEach(w -> {
            HDWallet hdWallet = new HDWallet(Util.decrypt(w.getSeedEncrypted(), secret), "");

            WalletDetailsDTO details = new WalletDetailsDTO();
            details.setWallet(hdWallet);

            Map<CoinType, CoinDTO> coins = assemblyCoins(hdWallet);
            details.setCoins(coins);

            wallets.put(w.getId(), details);

            addresses.addAll(coins.values().stream().map(c -> c.getAddress()).collect(Collectors.toSet()));
        });
    }

    public WalletDetailsDTO generateNewWallet() {
        HDWallet hdWallet = new HDWallet(128, "");

        WalletDetailsDTO details = new WalletDetailsDTO();
        details.setWallet(hdWallet);

        Map<CoinType, CoinDTO> coins = assemblyCoins(hdWallet);
        details.setCoins(coins);

        return details;
    }

    public String encrypt(String seed) {
        return Util.encrypt(seed, secret);
    }

    public String decrypt(String seedEncrypted) {
        return Util.decrypt(seedEncrypted, secret);
    }

    private Map<CoinType, CoinDTO> assemblyCoins(HDWallet hdWallet) {
        Map<CoinType, CoinDTO> coins = new HashMap<>();

        coins.put(CoinType.BITCOIN, new CoinDTO(CoinService.CoinEnum.BTC.name(), new BitcoinAddress(HDWallet.getPublicKeyFromExtended(getXpub(hdWallet, CoinType.BITCOIN), CoinType.BITCOIN, getPath(CoinType.BITCOIN)), CoinType.BITCOIN.p2pkhPrefix()).description(), hdWallet.getKeyForCoin(CoinType.BITCOIN)));
        coins.put(CoinType.BITCOINCASH, new CoinDTO(CoinService.CoinEnum.BCH.name(), CoinType.BITCOINCASH.deriveAddress(hdWallet.getKeyForCoin(CoinType.BITCOINCASH)), hdWallet.getKeyForCoin(CoinType.BITCOINCASH)));
        coins.put(CoinType.LITECOIN, new CoinDTO(CoinService.CoinEnum.LTC.name(), CoinType.LITECOIN.deriveAddress(hdWallet.getKeyForCoin(CoinType.LITECOIN)), hdWallet.getKeyForCoin(CoinType.LITECOIN)));
        coins.put(CoinType.DASH, new CoinDTO(CoinService.CoinEnum.DASH.name(), CoinType.DASH.deriveAddress(hdWallet.getKeyForCoin(CoinType.DASH)), hdWallet.getKeyForCoin(CoinType.DASH)));
        coins.put(CoinType.DOGECOIN, new CoinDTO(CoinService.CoinEnum.DOGE.name(), CoinType.DOGECOIN.deriveAddress(hdWallet.getKeyForCoin(CoinType.DOGECOIN)), hdWallet.getKeyForCoin(CoinType.DOGECOIN)));
        coins.put(CoinType.ETHEREUM, new CoinDTO(CoinService.CoinEnum.ETH.name(), CoinType.ETHEREUM.deriveAddress(hdWallet.getKeyForCoin(CoinType.ETHEREUM)), hdWallet.getKeyForCoin(CoinType.ETHEREUM)));
        coins.put(CoinType.BINANCE, new CoinDTO(CoinService.CoinEnum.BNB.name(), CoinType.BINANCE.deriveAddress(hdWallet.getKeyForCoin(CoinType.BINANCE)), hdWallet.getKeyForCoin(CoinType.BINANCE)));
        coins.put(CoinType.XRP, new CoinDTO(CoinService.CoinEnum.XRP.name(), CoinType.XRP.deriveAddress(hdWallet.getKeyForCoin(CoinType.XRP)), hdWallet.getKeyForCoin(CoinType.XRP)));
        coins.put(CoinType.TRON, new CoinDTO(CoinService.CoinEnum.TRX.name(), CoinType.TRON.deriveAddress(hdWallet.getKeyForCoin(CoinType.TRON)), hdWallet.getKeyForCoin(CoinType.TRON)));

        return coins;
    }

    public String getPath(CoinType coinType) {
        if (coinType == CoinType.BITCOIN) {
            return "m/44'/0'/0'/0/0";
        } else {
            return coinType.derivationPath();
        }
    }

    public String getPath(String address) {
        return coinPathRep.getCoinPathByAddress(address).getPath();
    }

    public String getXpub(HDWallet hdWallet, CoinType coinType) {
        if (coinType == CoinType.BITCOIN || coinType == CoinType.XRP) {
            return hdWallet.getExtendedPublicKey(Purpose.BIP44, coinType, HDVersion.XPUB);
        } else {
            return hdWallet.getExtendedPublicKey(coinType.purpose(), coinType, coinType.xpubVersion());
        }
    }

    public String generateNewPath(String path, Integer index) {
        return path.substring(0, path.length() - 1) + index;
    }

    public String getAddress(Long walletId, CoinType coinType, String newPath) {
        HDWallet hdWallet = get(walletId).getWallet();

        if (coinType == CoinType.BITCOIN) {
            PublicKey publicKey = HDWallet.getPublicKeyFromExtended(getXpub(hdWallet, coinType), CoinType.BITCOIN, newPath);

            return new BitcoinAddress(publicKey, CoinType.BITCOIN.p2pkhPrefix()).description();
        } else if (coinType == CoinType.BITCOINCASH || coinType == CoinType.LITECOIN) {
            PublicKey publicKey = HDWallet.getPublicKeyFromExtended(getXpub(hdWallet, coinType), coinType, newPath);

            return coinType.deriveAddressFromPublicKey(publicKey);
        } else if (coinType == CoinType.XRP) {
            PublicKey publicKey = HDWallet.getPublicKeyFromExtended(getXpub(hdWallet, coinType), coinType, newPath);

            return new AnyAddress(publicKey, coinType).description();
        } else if (coinType == CoinType.ETHEREUM) {
            PrivateKey privateKey = get(walletId).getWallet().getKey(coinType, newPath);

            return new AnyAddress(privateKey.getPublicKeySecp256k1(false), coinType).description();
        } else if (coinType == CoinType.TRON) {
            PrivateKey privateKey = get(walletId).getWallet().getKey(coinType, newPath);

            return new AnyAddress(privateKey.getPublicKeySecp256k1(false), coinType).description();
        } else if (coinType == CoinType.BINANCE) {
            PrivateKey privateKey = get(walletId).getWallet().getKey(coinType, newPath);

            return new AnyAddress(privateKey.getPublicKeySecp256k1(true), coinType).description();
        }

        return null;
    }

    public String getReceivingAddress(Long walletId, CoinService.CoinEnum coin) {
        try {
            CoinType coinType = coin.getCoinType();
            Coin coinEntity = coin.getCoinEntity();

            CoinPath freeCoinPath = coinPathRep.getFreeCoinPath(walletId, coinEntity.getId(), Constant.HOURS_BETWEEN_TRANSACTIONS);
            String address = null;

            //there is no free already generated addresses so need to generate a new one
            if (freeCoinPath == null) {
                Integer index = coinPathRep.countCoinPathByCoin(coinEntity);
                Wallet wallet = walletRep.findById(walletId).get();

                String path = getPath(coinType);
                String newPath = generateNewPath(path, index + 1);
                address = getAddress(walletId, coinType, newPath);

                CoinPath coinPath = new CoinPath();
                coinPath.setPath(newPath);
                coinPath.setAddress(address);
                coinPath.setCoin(coinEntity);
                coinPath.setWallet(wallet);
                coinPathRep.save(coinPath);
            } else {
                freeCoinPath.setUpdateDate(new Date());
                coinPathRep.save(freeCoinPath);

                address = freeCoinPath.getAddress();
            }

            if (coin == CoinService.CoinEnum.ETH) {
                gethService.addAddressToJournal(address);
            }

            return address;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public BigDecimal getBalance(CoinService.CoinEnum coin, String address) {
        try {
            BigDecimal balance = coin.getBalance(address);

            BigDecimal pendingSum = coin.getNodeTransactions(address).values().stream()
                    .filter(e -> e.getType() == TransactionType.WITHDRAW.getValue() && e.getStatus() == TransactionStatus.PENDING.getValue())
                    .map(TransactionDetailsDTO::getCryptoAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            pendingSum = pendingSum.add(coin.getTxFee());

            if (coin == CoinService.CoinEnum.XRP) {
                pendingSum = pendingSum.add(new BigDecimal(20));
            }

            return BigDecimal.ZERO.max(balance.subtract(pendingSum));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return BigDecimal.ZERO;
    }

    public boolean isEnoughBalance(CoinService.CoinEnum coin, String address, BigDecimal amount) {
        BigDecimal balance = getBalance(coin, address);

        if (coin == CoinService.CoinEnum.CATM || coin == CoinService.CoinEnum.USDC) {
            BigDecimal ethBalance = getBalance(CoinService.CoinEnum.ETH, address);
            BigDecimal fee = convertToFee(coin);

            return balance.compareTo(amount.add(fee)) >= 0 && ethBalance.compareTo(BigDecimal.ZERO) > 0;
        }

        return balance.compareTo(BigDecimal.ZERO) > 0;
    }

    public Map<String, List<String>> getReceivingAddressesTxs(CoinService.CoinEnum coinCode, List<String> addresses) {
        Map<String, List<String>> map = new HashMap<>();

        addresses.stream().forEach(e -> {
            List<String> txIds = coinCode.getNodeTransactions(e).entrySet().stream()
                    .filter(x -> x.getValue().getToAddress().equalsIgnoreCase(e) && x.getValue().getTimestamp() + Constant.HOURS_BETWEEN_TRANSACTIONS * 3600000 >= System.currentTimeMillis())
                    .map(x -> x.getKey()).collect(Collectors.toList());

            map.put(e, txIds);
        });

        return map;
    }

    public String transfer(Long walletId, CoinService.CoinEnum coin, String fromAddress, String toAddress, BigDecimal amount) {
        try {
            BigDecimal balance = getBalance(coin, fromAddress);

            if (balance.compareTo(amount) >= 0 && amount.compareTo(BigDecimal.ZERO) > 0) {
                String hex = coin.sign(walletId, fromAddress, toAddress, amount);

                TransactionDTO dto = new TransactionDTO();
                dto.setHex(hex);
                dto.setFromAddress(fromAddress);
                dto.setToAddress(toAddress);
                dto.setCryptoAmount(amount);

                String txId = coin.submitTransaction(dto);

                if (StringUtils.isNotBlank(txId)) {
                    persist(coin, fromAddress, toAddress, amount, txId);

                    return txId;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private void persist(CoinService.CoinEnum coin, String fromAddress, String toAddress, BigDecimal amount, String txId) {
        try {
            TransactionDetailsDTO tx = new TransactionDetailsDTO();
            tx.setTxId(txId);
            tx.setCoin(coin.name());
            tx.setCryptoAmount(amount);
            tx.setFromAddress(fromAddress);
            tx.setToAddress(toAddress);

            if (isServerAddress(fromAddress)) {
                tx.setType(TransactionType.SELL.getValue());
            } else {
                tx.setType(TransactionType.MOVE.getValue());
            }

            tx.setStatus(coin.getTransactionDetails(txId, StringUtils.EMPTY).getStatus());

            mongo.save(tx);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isServerAddress(String address) {
        return addresses.contains(address);
    }

    public BigDecimal convertToFee(CoinService.CoinEnum toCoin) {
        return toCoin.getTxFee().multiply(CoinService.CoinEnum.ETH.getPrice()).divide(toCoin.getPrice(), toCoin.getCoinEntity().getScale(), BigDecimal.ROUND_DOWN).stripTrailingZeros();
    }

    public WalletDetailsDTO get(Long walletId) {
        return wallets.get(walletId);
    }
}