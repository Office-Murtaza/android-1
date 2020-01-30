// Copyright © 2017-2019 Trust Wallet.
//
// This file is part of Trust. The full Trust copyright notice, including
// terms governing use, modification, and redistribution, is contained in the
// file LICENSE at the root of the source code distribution tree.
//
// This is a GENERATED FILE, changes made here WILL BE LOST.
//

package wallet.core.jni;


public enum PublicKeyType {
    SECP256K1 (0),
    SECP256K1EXTENDED (1),
    NIST256P1 (2),
    NIST256P1EXTENDED (3),
    ED25519 (4),
    ED25519BLAKE2B (5),
    CURVE25519 (6);

    private final int value;
    PublicKeyType(int value) {
        this.value = value;
    }
    public int value() { return value; }

    public static PublicKeyType createFromValue(int value) {
        switch (value) {
            case 0: return PublicKeyType.SECP256K1;
            case 1: return PublicKeyType.SECP256K1EXTENDED;
            case 2: return PublicKeyType.NIST256P1;
            case 3: return PublicKeyType.NIST256P1EXTENDED;
            case 4: return PublicKeyType.ED25519;
            case 5: return PublicKeyType.ED25519BLAKE2B;
            case 6: return PublicKeyType.CURVE25519;
            default: return null;
        }
    }

}
