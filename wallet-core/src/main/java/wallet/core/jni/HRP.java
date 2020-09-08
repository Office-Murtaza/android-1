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
    BITCOIN (1),
    LITECOIN (2),
    VIACOIN (3),
    GROESTLCOIN (4),
    DIGIBYTE (5),
    MONACOIN (6),
    COSMOS (7),
    BITCOINCASH (8),
    BITCOINGOLD (9),
    IOTEX (10),
    ZILLIQA (11),
    TERRA (12),
    KAVA (13),
    BANDCHAIN (14),
    ELROND (15),
    BINANCE (16),
    HARMONY (17),
    CARDANO (18),
    QTUM (19);

    private final int value;
    HRP(int value) {
        this.value = value;
    }
    public int value() { return value; }

    public static HRP createFromValue(int value) {
        switch (value) {
            case 0: return HRP.UNKNOWN;
            case 1: return HRP.BITCOIN;
            case 2: return HRP.LITECOIN;
            case 3: return HRP.VIACOIN;
            case 4: return HRP.GROESTLCOIN;
            case 5: return HRP.DIGIBYTE;
            case 6: return HRP.MONACOIN;
            case 7: return HRP.COSMOS;
            case 8: return HRP.BITCOINCASH;
            case 9: return HRP.BITCOINGOLD;
            case 10: return HRP.IOTEX;
            case 11: return HRP.ZILLIQA;
            case 12: return HRP.TERRA;
            case 13: return HRP.KAVA;
            case 14: return HRP.BANDCHAIN;
            case 15: return HRP.ELROND;
            case 16: return HRP.BINANCE;
            case 17: return HRP.HARMONY;
            case 18: return HRP.CARDANO;
            case 19: return HRP.QTUM;
            default: return null;
        }
    }


    public String toString() {
        switch (this) {
        case UNKNOWN: return "";
        case BITCOIN: return "bc";
        case LITECOIN: return "ltc";
        case VIACOIN: return "via";
        case GROESTLCOIN: return "grs";
        case DIGIBYTE: return "dgb";
        case MONACOIN: return "mona";
        case COSMOS: return "cosmos";
        case BITCOINCASH: return "bitcoincash";
        case BITCOINGOLD: return "btg";
        case IOTEX: return "io";
        case ZILLIQA: return "zil";
        case TERRA: return "terra";
        case KAVA: return "kava";
        case BANDCHAIN: return "band";
        case ELROND: return "erd";
        case BINANCE: return "bnb";
        case HARMONY: return "one";
        case CARDANO: return "addr";
        case QTUM: return "qc";
        default: return "";
        }
    }
}
