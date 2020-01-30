// Copyright © 2017-2019 Trust Wallet.
//
// This file is part of Trust. The full Trust copyright notice, including
// terms governing use, modification, and redistribution, is contained in the
// file LICENSE at the root of the source code distribution tree.
//
// This is a GENERATED FILE, changes made here WILL BE LOST.
//

package wallet.core.jni;

import java.security.InvalidParameterException;

public class X509 {
    private byte[] bytes;

    private X509() {
    }

    static X509 createFromNative(byte[] bytes) {
        X509 instance = new X509();
        instance.bytes = bytes;
        return instance;
    }


    public static native byte[] encodeED25519PublicKey(byte[] publicKey);
    public static native byte[] decodeED25519PublicKey(byte[] data);

}
