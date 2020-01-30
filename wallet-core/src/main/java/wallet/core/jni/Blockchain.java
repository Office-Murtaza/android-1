// Copyright © 2017-2019 Trust Wallet.
//
// This file is part of Trust. The full Trust copyright notice, including
// terms governing use, modification, and redistribution, is contained in the
// file LICENSE at the root of the source code distribution tree.
//
// This is a GENERATED FILE, changes made here WILL BE LOST.
//

package wallet.core.jni;


public enum Blockchain {
    BITCOIN (0),
    ETHEREUM (1),
    WANCHAIN (2),
    VECHAIN (3),
    TRON (4),
    ICON (5),
    BINANCE (6),
    RIPPLE (7),
    TEZOS (8),
    NIMIQ (9),
    STELLAR (10),
    AION (11),
    COSMOS (12),
    THETA (13),
    ONTOLOGY (14),
    ZILLIQA (15),
    IOTEX (16),
    ARK (17),
    EOS (18),
    IOST (19),
    SEMUX (20),
    NANO (21),
    NEO (22),
    STEEM (23),
    WAVES (25),
    AETERNITY (26),
    NEBULAS (27),
    FIO (28),
    SOLANA (29),
    HARMONY (30),
    NEAR (31),
    ALGORAND (32);

    private final int value;
    Blockchain(int value) {
        this.value = value;
    }
    public int value() { return value; }

    public static Blockchain createFromValue(int value) {
        switch (value) {
            case 0: return Blockchain.BITCOIN;
            case 1: return Blockchain.ETHEREUM;
            case 2: return Blockchain.WANCHAIN;
            case 3: return Blockchain.VECHAIN;
            case 4: return Blockchain.TRON;
            case 5: return Blockchain.ICON;
            case 6: return Blockchain.BINANCE;
            case 7: return Blockchain.RIPPLE;
            case 8: return Blockchain.TEZOS;
            case 9: return Blockchain.NIMIQ;
            case 10: return Blockchain.STELLAR;
            case 11: return Blockchain.AION;
            case 12: return Blockchain.COSMOS;
            case 13: return Blockchain.THETA;
            case 14: return Blockchain.ONTOLOGY;
            case 15: return Blockchain.ZILLIQA;
            case 16: return Blockchain.IOTEX;
            case 17: return Blockchain.ARK;
            case 18: return Blockchain.EOS;
            case 19: return Blockchain.IOST;
            case 20: return Blockchain.SEMUX;
            case 21: return Blockchain.NANO;
            case 22: return Blockchain.NEO;
            case 23: return Blockchain.STEEM;
            case 25: return Blockchain.WAVES;
            case 26: return Blockchain.AETERNITY;
            case 27: return Blockchain.NEBULAS;
            case 28: return Blockchain.FIO;
            case 29: return Blockchain.SOLANA;
            case 30: return Blockchain.HARMONY;
            case 31: return Blockchain.NEAR;
            case 32: return Blockchain.ALGORAND;
            default: return null;
        }
    }

}
