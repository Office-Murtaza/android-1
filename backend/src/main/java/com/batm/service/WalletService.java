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
        return coinType == CoinType.BITCOIN ? wallet.getExtendedPublicKey(Purpose.BIP44, coinType, HDVersion.XPUB) : wallet.getExtendedPublicKey(coinType.purpose(), coinType, coinType.xpubVersion());
    }

    public String generateNewAddress(CoinType coinType, String newPath) {
        PublicKey publicKey = HDWallet.getPublicKeyFromExtended(getXpub(coinType), newPath);
        String address = coinType == CoinType.BITCOIN ? new BitcoinAddress(publicKey, CoinType.BITCOIN.p2pkhPrefix()).description() : coinType.deriveAddressFromPublicKey(publicKey);

        return address;
    }

    public String generateNewPath(CoinType coinType, Integer lastIndex) {
        String path = coinType == CoinType.BITCOIN ? "m/44'/0'/0'/0/0" : coinType.derivationPath();

        return path.substring(0, path.length() - 1) + (lastIndex + 1);
    }
}