// Copyright © 2017-2019 Trust Wallet.
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
    COSMOS (4),
    DIGIBYTE (5),
    GROESTLCOIN (6),
    HARMONY (7),
    LITECOIN (8),
    MONACOIN (9),
    QTUM (10),
    TERRA (11),
    VIACOIN (12),
    ZILLIQA (13);

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
            case 4: return HRP.COSMOS;
            case 5: return HRP.DIGIBYTE;
            case 6: return HRP.GROESTLCOIN;
            case 7: return HRP.HARMONY;
            case 8: return HRP.LITECOIN;
            case 9: return HRP.MONACOIN;
            case 10: return HRP.QTUM;
            case 11: return HRP.TERRA;
            case 12: return HRP.VIACOIN;
            case 13: return HRP.ZILLIQA;
            default: return null;
        }
    }


    public String toString() {
        switch (this) {
        case UNKNOWN: return "";
        case BINANCE: return "bnb";
        case BITCOIN: return "bc";
        case BITCOINCASH: return "bitcoincash";
        case COSMOS: return "cosmos";
        case DIGIBYTE: return "dgb";
        case GROESTLCOIN: return "grs";
        case HARMONY: return "one";
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
