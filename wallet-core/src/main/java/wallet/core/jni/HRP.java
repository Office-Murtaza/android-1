// Copyright © 2017-2020 Trust Wallet.
//
// This file is part of Trust. The full Trust copyright notice, including
// terms governing use, modification, and redistribution, is contained in the
// file LICENSE at the root of the source code distribution tree.
//
// This is a GENERATED FILE, changes made here WILL BE LOST.
//

package wallet.core.jni;


public enum HRP {
    UNKNOWN (0),
    BINANCE (1),
    BITCOIN (2),
    BITCOINCASH (3),
    BITCOINGOLD (4),
    CARDANO (5),
    COSMOS (6),
    DIGIBYTE (7),
    ELROND (8),
    GROESTLCOIN (9),
    HARMONY (10),
    IOTEX (11),
    KAVA (12),
    LITECOIN (13),
    MONACOIN (14),
    QTUM (15),
    TERRA (16),
    VIACOIN (17),
    ZILLIQA (18);

    private final int value;
    HRP(int value) {
        this.value = value;
    }
    public int value() { return value; }

    public static HRP createFromValue(int value) {
        switch (value) {
            case 0: return HRP.UNKNOWN;
            case 1: return HRP.BINANCE;
            case 2: return HRP.BITCOIN;
            case 3: return HRP.BITCOINCASH;
            case 4: return HRP.BITCOINGOLD;
            case 5: return HRP.CARDANO;
            case 6: return HRP.COSMOS;
            case 7: return HRP.DIGIBYTE;
            case 8: return HRP.ELROND;
            case 9: return HRP.GROESTLCOIN;
            case 10: return HRP.HARMONY;
            case 11: return HRP.IOTEX;
            case 12: return HRP.KAVA;
            case 13: return HRP.LITECOIN;
            case 14: return HRP.MONACOIN;
            case 15: return HRP.QTUM;
            case 16: return HRP.TERRA;
            case 17: return HRP.VIACOIN;
            case 18: return HRP.ZILLIQA;
            default: return null;
        }
    }


    public String toString() {
        switch (this) {
        case UNKNOWN: return "";
        case BINANCE: return "bnb";
        case BITCOIN: return "bc";
        case BITCOINCASH: return "bitcoincash";
        case BITCOINGOLD: return "btg";
        case CARDANO: return "addr";
        case COSMOS: return "cosmos";
        case DIGIBYTE: return "dgb";
        case ELROND: return "erd";
        case GROESTLCOIN: return "grs";
        case HARMONY: return "one";
        case IOTEX: return "io";
        case KAVA: return "kava";
        case LITECOIN: return "ltc";
        case MONACOIN: return "mona";
        case QTUM: return "qc";
        case TERRA: return "terra";
        case VIACOIN: return "via";
        case ZILLIQA: return "zil";
        default: return "";
        }
    }
}
