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



    private PrivateKey privateKeyBTC = null;
    private PrivateKey privateKeyETH = null;
    private PrivateKey privateKeyXRP = null;
    private PrivateKey privateKeyTRX = null;
    private PrivateKey privateKeyBNB = null;

    @PostConstruct
    public void init() {
        String seed = "garage become kid awake salon forget minimum snack crash broken leaf genius";

        //wallet = new HDWallet(AES.decrypt(walletSeed, walletSeedKey), "");
        wallet = new HDWallet(seed, "");

        privateKeyBTC = wallet.getKeyForCoin(CoinType.BITCOIN);
        //String extPublicKeyBTC = wallet.getExtendedPublicKey(Purpose.BIP44, CoinType.BITCOIN, HDVersion.XPUB);
        PublicKey publicKeyBTC = HDWallet.getPublicKeyFromExtended(getXPUB(CoinType.BITCOIN), "m/44'/0'/0'/0/0");
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

    public String getXPUB(CoinType coinType) {
        if(coinType == CoinType.BITCOIN) {
            return wallet.getExtendedPublicKey(Purpose.BIP44, coinType, HDVersion.XPUB);
        } else {
            return wallet.getExtendedPublicKey(coinType.purpose(), coinType, coinType.xpubVersion());
        }
    }
}