// Copyright © 2017-2019 Trust Wallet.
//
// This file is part of Trust. The full Trust copyright notice, including
// terms governing use, modification, and redistribution, is contained in the
// file LICENSE at the root of the source code distribution tree.
//
// This is a GENERATED FILE, changes made here WILL BE LOST.
//

package wallet.core.jni;


public enum BravoAddressType {
    MAINNET (0),
    TESTNET (1);

    private final int value;
    BravoAddressType(int value) {
        this.value = value;
    }
    public int value() { return value; }

    public static BravoAddressType createFromValue(int value) {
        switch (value) {
            case 0: return BravoAddressType.MAINNET;
            case 1: return BravoAddressType.TESTNET;
            default: return null;
        }
    }

}
