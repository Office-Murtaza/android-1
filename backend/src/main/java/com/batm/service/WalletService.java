package com.batm.service;

import com.batm.dto.NodeTransactionsDTO;
import com.batm.dto.ReceivedAddressDTO;
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
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Getter
@Service
public class WalletService {

    static {
        System.loadLibrary("TrustWalletCore");
    }

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

    private List<String> serverAddresses = new ArrayList<>();

    @PostConstruct
    public void init() {
        wallet = new HDWallet(seed, "");

        privateKeyBTC = wallet.getKeyForCoin(CoinType.BITCOIN);
        PublicKey publicKeyBTC = wallet.getPublicKeyFromExtended(getXPUB(CoinType.BITCOIN), getPath(CoinType.BITCOIN));
        addressBTC = new BitcoinAddress(publicKeyBTC, CoinType.BITCOIN.p2pkhPrefix()).description();
        serverAddresses.add(addressBTC);

        PrivateKey privateKeyBCH = wallet.getKeyForCoin(CoinType.BITCOINCASH);
        addressBCH = CoinType.BITCOINCASH.deriveAddress(privateKeyBCH);
        serverAddresses.add(addressBCH);

        privateKeyETH = wallet.getKeyForCoin(CoinType.ETHEREUM);
        addressETH = CoinType.ETHEREUM.deriveAddress(privateKeyETH);
        serverAddresses.add(addressETH);

        PrivateKey privateKeyLTC = wallet.getKeyForCoin(CoinType.LITECOIN);
        addressLTC = CoinType.LITECOIN.deriveAddress(privateKeyLTC);
        serverAddresses.add(addressLTC);

        privateKeyBNB = wallet.getKeyForCoin(CoinType.BINANCE);
        addressBNB = CoinType.BINANCE.deriveAddress(privateKeyBNB);
        serverAddresses.add(addressBNB);

        privateKeyXRP = wallet.getKeyForCoin(CoinType.XRP);
        addressXRP = CoinType.XRP.deriveAddress(privateKeyXRP);
        serverAddresses.add(addressXRP);

        privateKeyTRX = wallet.getKeyForCoin(CoinType.TRON);
        addressTRX = CoinType.TRON.deriveAddress(privateKeyTRX);
        serverAddresses.add(addressTRX);
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

    public String generateNewAddress(CoinService.CoinEnum coinCode) {
        try {
            CoinType coinType = coinCode.getCoinType();
            Coin coinEntity = coinCode.getCoinEntity();

            CoinPath existingFreePath = coinPathRep.findFirstByCoinIdAndHoursAgo(coinEntity.getId(), 1);

            //there is no free already generated addresses so need to generate a new one
            if (existingFreePath == null) {
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

    public BigDecimal getBalance(CoinService.CoinEnum coinCode) {
        try {
            String walletAddress = coinCode.getWalletAddress();
            BigDecimal balance = coinCode.getBalance(walletAddress);
            NodeTransactionsDTO nodeTransactionsDTO = coinCode.getNodeTransactions(walletAddress);

            BigDecimal pendingSum = nodeTransactionsDTO.getMap().values().stream()
                    .filter(e -> e.getType() == TransactionType.WITHDRAW && e.getStatus() == TransactionStatus.PENDING)
                    .map(TransactionDTO::getCryptoAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            return balance.subtract(pendingSum);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public List<ReceivedAddressDTO> getReceivedAddresses(CoinService.CoinEnum coinCode, Set<String> addresses) {
        List<ReceivedAddressDTO> list = new ArrayList<>();

        for (String address : addresses) {
            ReceivedAddressDTO dto = new ReceivedAddressDTO();
            dto.setAddress(address);

            Map<String, TransactionDTO> map = coinCode.getNodeTransactions(address).getMap();

            Set<String> txIds = map.entrySet().stream()
                    .filter(x -> x.getValue().getToAddress().equalsIgnoreCase(address))
                    .map(x -> x.getKey()).collect(Collectors.toSet());

            dto.setTxIds(txIds);

            list.add(dto);
        }

        return list;
    }

    public String sendCoins(CoinService.CoinEnum coinCode, String fromAddress, String toAddress, BigDecimal amount) {
        try {
            String hex = coinCode.sign(fromAddress, toAddress, amount);
            String txId = coinCode.submitTransaction(hex);

            TransactionRecordWallet wallet = new TransactionRecordWallet();
            wallet.setCoin(coinCode.getCoinEntity());
            wallet.setAmount(amount);

            if (isServerAddress(fromAddress)) {
                wallet.setType(TransactionType.SELL.getValue());
            } else {
                wallet.setType(TransactionType.MOVE.getValue());
            }

            wallet.setTxId(txId);
            wallet.setStatus(coinCode.getTransactionStatus(txId).getValue());

            transactionRecordWalletRep.save(wallet);

            return txId;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public boolean isServerAddress(String address) {
        return serverAddresses.contains(address);
    }
}