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
    BANDCHAIN (1),
    BINANCE (2),
    BITCOIN (3),
    BITCOINCASH (4),
    BITCOINGOLD (5),
    CARDANO (6),
    COSMOS (7),
    DIGIBYTE (8),
    ELROND (9),
    GROESTLCOIN (10),
    HARMONY (11),
    IOTEX (12),
    KAVA (13),
    LITECOIN (14),
    MONACOIN (15),
    QTUM (16),
    TERRA (17),
    VIACOIN (18),
    ZILLIQA (19);

    private final int value;
    HRP(int value) {
        this.value = value;
    }
    public int value() { return value; }

    public static HRP createFromValue(int value) {
        switch (value) {
            case 0: return HRP.UNKNOWN;
            case 1: return HRP.BANDCHAIN;
            case 2: return HRP.BINANCE;
            case 3: return HRP.BITCOIN;
            case 4: return HRP.BITCOINCASH;
            case 5: return HRP.BITCOINGOLD;
            case 6: return HRP.CARDANO;
            case 7: return HRP.COSMOS;
            case 8: return HRP.DIGIBYTE;
            case 9: return HRP.ELROND;
            case 10: return HRP.GROESTLCOIN;
            case 11: return HRP.HARMONY;
            case 12: return HRP.IOTEX;
            case 13: return HRP.KAVA;
            case 14: return HRP.LITECOIN;
            case 15: return HRP.MONACOIN;
            case 16: return HRP.QTUM;
            case 17: return HRP.TERRA;
            case 18: return HRP.VIACOIN;
            case 19: return HRP.ZILLIQA;
            default: return null;
        }
    }


    public String toString() {
        switch (this) {
        case UNKNOWN: return "";
        case BANDCHAIN: return "band";
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
