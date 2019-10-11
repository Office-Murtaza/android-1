package com.batm.service;

import com.batm.util.AES;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import wallet.core.jni.*;
import javax.annotation.PostConstruct;

@Service
public class WalletService {

    static {
        System.loadLibrary("TrustWalletCore");
    }

    @Value("${wallet.seed}")
    private String walletSeed;

    @Value("${wallet.seed.key}")
    private String walletSeedKey;

    private HDWallet wallet = null;

    private String addressBTC = null;
    private String addressBCH = null;
    private String addressETH = null;
    private String addressLTC = null;
    private String addressBNB = null;
    private String addressXRP = null;
    private String addressTRX = null;

    @PostConstruct
    public void init() {
        wallet = new HDWallet(AES.decrypt(walletSeed, walletSeedKey), "");

        PrivateKey privateKeyBTC = wallet.getKeyForCoin(CoinType.BITCOIN);
        PublicKey publicKeyBTC = privateKeyBTC.getPublicKeySecp256k1(true);
        addressBTC = new BitcoinAddress(publicKeyBTC, P2PKHPrefix.BITCOIN.value()).description();

        PrivateKey privateKeyBCH = wallet.getKeyForCoin(CoinType.BITCOINCASH);
        addressBCH = CoinType.BITCOINCASH.deriveAddress(privateKeyBCH);

        PrivateKey privateKeyETH = wallet.getKeyForCoin(CoinType.ETHEREUM);
        addressETH = CoinType.ETHEREUM.deriveAddress(privateKeyETH);

        PrivateKey privateKeyLTC = wallet.getKeyForCoin(CoinType.LITECOIN);
        addressLTC = CoinType.LITECOIN.deriveAddress(privateKeyLTC);

        PrivateKey privateKeyBNB = wallet.getKeyForCoin(CoinType.BINANCE);
        addressBNB = CoinType.BINANCE.deriveAddress(privateKeyBNB);

        PrivateKey privateKeyXRP = wallet.getKeyForCoin(CoinType.XRP);
        addressXRP = CoinType.XRP.deriveAddress(privateKeyXRP);

        PrivateKey privateKeyTRX = wallet.getKeyForCoin(CoinType.TRON);
        addressTRX = CoinType.TRON.deriveAddress(privateKeyTRX);
    }

    public String getAddressBTC() {
        return addressBTC;
    }

    public String getAddressBCH() {
        return addressBCH;
    }

    public String getAddressETH() {
        return addressETH;
    }

    public String getAddressLTC() {
        return addressLTC;
    }

    public String getAddressBNB() {
        return addressBNB;
    }

    public String getAddressXRP() {
        return addressXRP;
    }

    public String getAddressTRX() {
        return addressTRX;
    }
}