// Copyright © 2017-2019 Trust Wallet.
//
// This file is part of Trust. The full Trust copyright notice, including
// terms governing use, modification, and redistribution, is contained in the
// file LICENSE at the root of the source code distribution tree.
//
// This is a GENERATED FILE, changes made here WILL BE LOST.
//

package wallet.core.jni;


public enum Curve {
    SECP256K1 (0),
    ED25519 (1),
    ED25519BLAKE2BNANO (2),
    CURVE25519 (3),
    NIST256P1 (4);

    private final int value;
    Curve(int value) {
        this.value = value;
    }
    public int value() { return value; }

    public static Curve createFromValue(int value) {
        switch (value) {
            case 0: return Curve.SECP256K1;
            case 1: return Curve.ED25519;
            case 2: return Curve.ED25519BLAKE2BNANO;
            case 3: return Curve.CURVE25519;
            case 4: return Curve.NIST256P1;
            default: return null;
        }
    }


    public String toString() {
        switch (this) {
        case SECP256K1: return "secp256k1";
        case ED25519: return "ed25519";
        case ED25519BLAKE2BNANO: return "ed25519-blake2b-nano";
        case CURVE25519: return "curve25519";
        case NIST256P1: return "nist256p1";
        default: return "";
        }
    }
}
