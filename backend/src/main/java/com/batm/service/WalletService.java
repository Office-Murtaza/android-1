package com.batm.service;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import wallet.core.jni.*;
import javax.annotation.PostConstruct;

@Getter
@Service
public class WalletService {

    static {
        System.loadLibrary("TrustWalletCore");
    }

    @Value("${wallet.seed}")
    private String seed;

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
        wallet = new HDWallet(seed, "");

        privateKeyBTC = wallet.getKeyForCoin(CoinType.BITCOIN);
        PublicKey publicKeyBTC = HDWallet.getPublicKeyFromExtended(getXpub(CoinType.BITCOIN), "m/44'/0'/0'/0/0");
        addressBTC = new BitcoinAddress(publicKeyBTC, CoinType.BITCOIN.p2pkhPrefix()).description();

        PrivateKey privateKeyBCH = wallet.getKeyForCoin(CoinType.BITCOINCASH);
        addressBCH = CoinType.BITCOINCASH.deriveAddress(privateKeyBCH);

        privateKeyETH = wallet.getKeyForCoin(CoinType.ETHEREUM);
        addressETH = CoinType.ETHEREUM.deriveAddress(privateKeyETH);

        PrivateKey privateKeyLTC = wallet.getKeyForCoin(CoinType.LITECOIN);
        addressLTC = CoinType.LITECOIN.deriveAddress(privateKeyLTC);

        privateKeyBNB = wallet.getKeyForCoin(CoinType.BINANCE);
        addressBNB = CoinType.BINANCE.deriveAddress(privateKeyBNB);

        privateKeyXRP = wallet.getKeyForCoin(CoinType.XRP);
        addressXRP = CoinType.XRP.deriveAddress(privateKeyXRP);

        privateKeyTRX = wallet.getKeyForCoin(CoinType.TRON);
        addressTRX = CoinType.TRON.deriveAddress(privateKeyTRX);
    }

    public String getXpub(CoinType coinType) {
        if (coinType == CoinType.BITCOIN) {
            return wallet.getExtendedPublicKey(Purpose.BIP44, coinType, HDVersion.XPUB);
        } else {
            return wallet.getExtendedPublicKey(coinType.purpose(), coinType, coinType.xpubVersion());
        }
    }
}