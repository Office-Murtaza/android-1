// Copyright © 2017-2020 Trust Wallet.
//
// This file is part of Trust. The full Trust copyright notice, including
// terms governing use, modification, and redistribution, is contained in the
// file LICENSE at the root of the source code distribution tree.
//
// This is a GENERATED FILE, changes made here WILL BE LOST.
//

package wallet.core.jni;


public enum CoinType {
    AETERNITY (457),
    AION (425),
    BINANCE (714),
    BITCOIN (0),
    BITCOINCASH (145),
    BITCOINGOLD (156),
    CALLISTO (820),
    CARDANO (1815),
    COSMOS (118),
    DASH (5),
    DECRED (42),
    DIGIBYTE (20),
    DOGECOIN (3),
    EOS (194),
    ETHEREUM (60),
    ETHEREUMCLASSIC (61),
    FIO (235),
    GOCHAIN (6060),
    GROESTLCOIN (17),
    ICON (74),
    IOTEX (304),
    KAVA (459),
    KIN (2017),
    LITECOIN (2),
    MONACOIN (22),
    NEBULAS (2718),
    NULS (8964),
    NANO (165),
    NEAR (397),
    NIMIQ (242),
    ONTOLOGY (1024),
    POANETWORK (178),
    QTUM (2301),
    XRP (144),
    SOLANA (501),
    STELLAR (148),
    TON (396),
    TEZOS (1729),
    THETA (500),
    THUNDERTOKEN (1001),
    NEO (888),
    TOMOCHAIN (889),
    TRON (195),
    VECHAIN (818),
    VIACOIN (14),
    WANCHAIN (5718350),
    ZCASH (133),
    ZCOIN (136),
    ZILLIQA (313),
    ZELCASH (19167),
    RAVENCOIN (175),
    WAVES (5741564),
    TERRA (330),
    HARMONY (1023),
    ALGORAND (283),
    KUSAMA (434),
    POLKADOT (354),
    FILECOIN (461),
    ELROND (508);

    private final int value;
    CoinType(int value) {
        this.value = value;
    }
    public int value() { return value; }

    public static CoinType createFromValue(int value) {
        switch (value) {
            case 457: return CoinType.AETERNITY;
            case 425: return CoinType.AION;
            case 714: return CoinType.BINANCE;
            case 0: return CoinType.BITCOIN;
            case 145: return CoinType.BITCOINCASH;
            case 156: return CoinType.BITCOINGOLD;
            case 820: return CoinType.CALLISTO;
            case 1815: return CoinType.CARDANO;
            case 118: return CoinType.COSMOS;
            case 5: return CoinType.DASH;
            case 42: return CoinType.DECRED;
            case 20: return CoinType.DIGIBYTE;
            case 3: return CoinType.DOGECOIN;
            case 194: return CoinType.EOS;
            case 60: return CoinType.ETHEREUM;
            case 61: return CoinType.ETHEREUMCLASSIC;
            case 235: return CoinType.FIO;
            case 6060: return CoinType.GOCHAIN;
            case 17: return CoinType.GROESTLCOIN;
            case 74: return CoinType.ICON;
            case 304: return CoinType.IOTEX;
            case 459: return CoinType.KAVA;
            case 2017: return CoinType.KIN;
            case 2: return CoinType.LITECOIN;
            case 22: return CoinType.MONACOIN;
            case 2718: return CoinType.NEBULAS;
            case 8964: return CoinType.NULS;
            case 165: return CoinType.NANO;
            case 397: return CoinType.NEAR;
            case 242: return CoinType.NIMIQ;
            case 1024: return CoinType.ONTOLOGY;
            case 178: return CoinType.POANETWORK;
            case 2301: return CoinType.QTUM;
            case 144: return CoinType.XRP;
            case 501: return CoinType.SOLANA;
            case 148: return CoinType.STELLAR;
            case 396: return CoinType.TON;
            case 1729: return CoinType.TEZOS;
            case 500: return CoinType.THETA;
            case 1001: return CoinType.THUNDERTOKEN;
            case 888: return CoinType.NEO;
            case 889: return CoinType.TOMOCHAIN;
            case 195: return CoinType.TRON;
            case 818: return CoinType.VECHAIN;
            case 14: return CoinType.VIACOIN;
            case 5718350: return CoinType.WANCHAIN;
            case 133: return CoinType.ZCASH;
            case 136: return CoinType.ZCOIN;
            case 313: return CoinType.ZILLIQA;
            case 19167: return CoinType.ZELCASH;
            case 175: return CoinType.RAVENCOIN;
            case 5741564: return CoinType.WAVES;
            case 330: return CoinType.TERRA;
            case 1023: return CoinType.HARMONY;
            case 283: return CoinType.ALGORAND;
            case 434: return CoinType.KUSAMA;
            case 354: return CoinType.POLKADOT;
            case 461: return CoinType.FILECOIN;
            case 508: return CoinType.ELROND;
            default: return null;
        }
    }

    public native Blockchain blockchain();
    public native Purpose purpose();
    public native Curve curve();
    public native HDVersion xpubVersion();
    public native HDVersion xprvVersion();
    public native HRP hrp();
    public native byte p2pkhPrefix();
    public native byte p2shPrefix();
    public native byte staticPrefix();
    public native boolean validate(String address);
    public native String derivationPath();
    public native String deriveAddress(PrivateKey privateKey);
    public native String deriveAddressFromPublicKey(PublicKey publicKey);
}
