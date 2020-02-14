// Copyright © 2017-2019 Trust Wallet.
//
// This file is part of Trust. The full Trust copyright notice, including
// terms governing use, modification, and redistribution, is contained in the
// file LICENSE at the root of the source code distribution tree.
//
// This is a GENERATED FILE, changes made here WILL BE LOST.
//

package wallet.core.jni;


public enum EOSKeyType {
    LEGACY (0),
    MODERNK1 (1),
    MODERNR1 (2);

    private final int value;
    EOSKeyType(int value) {
        this.value = value;
    }
    public int value() { return value; }

    public static EOSKeyType createFromValue(int value) {
        switch (value) {
            case 0: return EOSKeyType.LEGACY;
            case 1: return EOSKeyType.MODERNK1;
            case 2: return EOSKeyType.MODERNR1;
            default: return null;
        }
    }

}
