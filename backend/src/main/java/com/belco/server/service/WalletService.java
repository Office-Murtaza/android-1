package com.belco.server.service;

import com.belco.server.dto.CoinDTO;
import com.belco.server.dto.SubmitTransactionDTO;
import com.belco.server.dto.TransactionDetailsDTO;
import com.belco.server.entity.Coin;
import com.belco.server.entity.CoinPath;
import com.belco.server.entity.TransactionRecordWallet;
import com.belco.server.model.TransactionStatus;
import com.belco.server.model.TransactionType;
import com.belco.server.repository.CoinPathRep;
import com.belco.server.repository.TransactionRecordWalletRep;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import wallet.core.jni.*;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Getter
@Service
public class WalletService {

    static {
        System.loadLibrary("TrustWalletCore");
    }

    private final CoinPathRep coinPathRep;
    private final TransactionRecordWalletRep transactionRecordWalletRep;
    private final Map<CoinType, CoinDTO> coinsMap = new HashMap<>();

    @Value("${wallet.seed}")
    private String seed;
    private HDWallet wallet = null;

    public WalletService(CoinPathRep coinPathRep, TransactionRecordWalletRep transactionRecordWalletRep) {
        this.coinPathRep = coinPathRep;
        this.transactionRecordWalletRep = transactionRecordWalletRep;
        this.wallet = new HDWallet(seed, "");

        coinsMap.put(CoinType.BITCOIN, new CoinDTO(CoinService.CoinEnum.BTC.name(), new BitcoinAddress(HDWallet.getPublicKeyFromExtended(getXpub(CoinType.BITCOIN), CoinType.BITCOIN, getPath(CoinType.BITCOIN)), CoinType.BITCOIN.p2pkhPrefix()).description(), wallet.getKeyForCoin(CoinType.BITCOIN)));
        coinsMap.put(CoinType.BITCOINCASH, new CoinDTO(CoinService.CoinEnum.BCH.name(), CoinType.BITCOINCASH.deriveAddress(wallet.getKeyForCoin(CoinType.BITCOINCASH)), wallet.getKeyForCoin(CoinType.BITCOINCASH)));
        coinsMap.put(CoinType.LITECOIN, new CoinDTO(CoinService.CoinEnum.LTC.name(), CoinType.LITECOIN.deriveAddress(wallet.getKeyForCoin(CoinType.LITECOIN)), wallet.getKeyForCoin(CoinType.LITECOIN)));
        coinsMap.put(CoinType.DASH, new CoinDTO(CoinService.CoinEnum.DASH.name(), CoinType.DASH.deriveAddress(wallet.getKeyForCoin(CoinType.DASH)), wallet.getKeyForCoin(CoinType.DASH)));
        coinsMap.put(CoinType.DOGECOIN, new CoinDTO(CoinService.CoinEnum.DOGE.name(), CoinType.DOGECOIN.deriveAddress(wallet.getKeyForCoin(CoinType.DOGECOIN)), wallet.getKeyForCoin(CoinType.DOGECOIN)));
        coinsMap.put(CoinType.ETHEREUM, new CoinDTO(CoinService.CoinEnum.ETH.name(), CoinType.ETHEREUM.deriveAddress(wallet.getKeyForCoin(CoinType.ETHEREUM)), wallet.getKeyForCoin(CoinType.ETHEREUM)));
        coinsMap.put(CoinType.BINANCE, new CoinDTO(CoinService.CoinEnum.BNB.name(), CoinType.BINANCE.deriveAddress(wallet.getKeyForCoin(CoinType.BINANCE)), wallet.getKeyForCoin(CoinType.BINANCE)));
        coinsMap.put(CoinType.XRP, new CoinDTO(CoinService.CoinEnum.XRP.name(), CoinType.XRP.deriveAddress(wallet.getKeyForCoin(CoinType.XRP)), wallet.getKeyForCoin(CoinType.XRP)));
        coinsMap.put(CoinType.TRON, new CoinDTO(CoinService.CoinEnum.TRX.name(), CoinType.TRON.deriveAddress(wallet.getKeyForCoin(CoinType.TRON)), wallet.getKeyForCoin(CoinType.TRON)));
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

    public String getXpub(CoinType coinType) {
        if (coinType == CoinType.BITCOIN || coinType == CoinType.XRP) {
            return wallet.getExtendedPublicKey(Purpose.BIP44, coinType, HDVersion.XPUB);
        } else {
            return wallet.getExtendedPublicKey(coinType.purpose(), coinType, coinType.xpubVersion());
        }
    }

    public String generateNewPath(String path, Integer index) {
        return path.substring(0, path.length() - 1) + index;
    }

    public String getAddress(CoinType coinType, String newPath) {
        if (coinType == CoinType.BITCOIN) {
            PublicKey publicKey = HDWallet.getPublicKeyFromExtended(getXpub(coinType), CoinType.BITCOIN, newPath);

            return new BitcoinAddress(publicKey, CoinType.BITCOIN.p2pkhPrefix()).description();
        } else if (coinType == CoinType.BITCOINCASH || coinType == CoinType.LITECOIN) {
            PublicKey publicKey = HDWallet.getPublicKeyFromExtended(getXpub(coinType), coinType, newPath);

            return coinType.deriveAddressFromPublicKey(publicKey);
        } else if (coinType == CoinType.XRP) {
            PublicKey publicKey = HDWallet.getPublicKeyFromExtended(getXpub(coinType), coinType, newPath);

            return new AnyAddress(publicKey, coinType).description();
        } else if (coinType == CoinType.ETHEREUM) {
            PrivateKey privateKey = wallet.getKey(coinType, newPath);

            return new AnyAddress(privateKey.getPublicKeySecp256k1(false), coinType).description();
        } else if (coinType == CoinType.TRON) {
            PrivateKey privateKey = wallet.getKey(coinType, newPath);

            return new AnyAddress(privateKey.getPublicKeySecp256k1(false), coinType).description();
        } else if (coinType == CoinType.BINANCE) {
            PrivateKey privateKey = wallet.getKey(coinType, newPath);

            return new AnyAddress(privateKey.getPublicKeySecp256k1(true), coinType).description();
        }

        return null;
    }

    public String getReceivingAddress(CoinService.CoinEnum coin) {
        try {
            CoinType coinType = coin.getCoinType();
            Coin coinEntity = coin.getCoinEntity();

            CoinPath existingFreePath = coinPathRep.findFirstByCoinIdAndHoursAgo(coinEntity.getId(), 1);

            //there is no free already generated addresses so need to generate a new one
            if (existingFreePath == null) {
                Integer index = coinPathRep.countCoinPathByCoin(coinEntity);

                String path = getPath(coinType);
                String newPath = generateNewPath(path, index + 1);
                String address = getAddress(coinType, newPath);

                CoinPath coinPath = new CoinPath();
                coinPath.setPath(newPath);
                coinPath.setAddress(address);
                coinPath.setCoin(coinEntity);
                coinPathRep.save(coinPath);

                return address;
            } else {
                existingFreePath.setUpdateDate(new Date());
                coinPathRep.save(existingFreePath);

                return existingFreePath.getAddress();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public BigDecimal getBalance(CoinService.CoinEnum coin) {
        try {
            String walletAddress = coin.getWalletAddress();
            BigDecimal balance = coin.getBalance(walletAddress);

            BigDecimal pendingSum = coin.getNodeTransactions(walletAddress).values().stream()
                    .filter(e -> e.getType() == TransactionType.WITHDRAW && e.getStatus() == TransactionStatus.PENDING)
                    .map(TransactionDetailsDTO::getCryptoAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            if (coin == CoinService.CoinEnum.XRP) {
                pendingSum = pendingSum.add(new BigDecimal(20));
            }

            return balance.subtract(pendingSum);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return BigDecimal.ZERO;
    }

    public boolean isEnoughBalance(CoinService.CoinEnum coin, BigDecimal amount) {
        BigDecimal balance = getBalance(coin);

        if (coin == CoinService.CoinEnum.CATM || coin == CoinService.CoinEnum.USDT) {
            BigDecimal ethBalance = getBalance(CoinService.CoinEnum.ETH);
            BigDecimal fee = convertToFee(coin);

            return balance.compareTo(amount.add(fee)) >= 0 && ethBalance.compareTo(coin.getTxFee()) >= 0;
        }

        return balance.compareTo(amount.add(coin.getTxFee())) >= 0;
    }

    public Map<String, List<String>> getReceivingAddressesTxs(CoinService.CoinEnum coinCode, List<String> addresses) {
        Map<String, List<String>> map = new HashMap<>();

        addresses.stream().forEach(e -> {
            List<String> txIds = coinCode.getNodeTransactions(e).entrySet().stream()
                    .filter(x -> x.getValue().getToAddress().equalsIgnoreCase(e))
                    .map(x -> x.getKey()).collect(Collectors.toList());

            map.put(e, txIds);
        });

        return map;
    }

    public String transfer(CoinService.CoinEnum coin, String fromAddress, String toAddress, BigDecimal amount) {
        try {
            BigDecimal balance = getBalance(coin);

            if (balance.compareTo(amount) >= 0 && amount.compareTo(BigDecimal.ZERO) > 0) {
                String hex = coin.sign(fromAddress, toAddress, amount);

                SubmitTransactionDTO dto = new SubmitTransactionDTO();
                dto.setHex(hex);
                dto.setFromAddress(fromAddress);
                dto.setToAddress(toAddress);
                dto.setCryptoAmount(amount);
                dto.setFee(BigDecimal.ZERO);

                String txId = coin.submitTransaction(dto);

                TransactionRecordWallet wallet = new TransactionRecordWallet();
                wallet.setCoin(coin.getCoinEntity());
                wallet.setAmount(amount);

                if (isServerAddress(coin.getCoinType(), fromAddress)) {
                    wallet.setType(TransactionType.SELL.getValue());
                } else {
                    wallet.setType(TransactionType.MOVE.getValue());
                }

                wallet.setTxId(txId);
                wallet.setStatus(coin.getTransactionDetails(txId, StringUtils.EMPTY).getStatus().getValue());

                transactionRecordWalletRep.save(wallet);

                return txId;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public boolean isServerAddress(CoinType coinType, String address) {
        return coinsMap.get(coinType).getAddress().equalsIgnoreCase(address);
    }

    public BigDecimal convertToFee(CoinService.CoinEnum toCoin) {
        return toCoin.getTxFee().multiply(CoinService.CoinEnum.ETH.getPrice()).divide(toCoin.getPrice(), toCoin.getCoinEntity().getScale(), BigDecimal.ROUND_DOWN).stripTrailingZeros();
    }
}