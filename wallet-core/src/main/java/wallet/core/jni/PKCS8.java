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

public class PKCS8 {
    private byte[] bytes;

    private PKCS8() {
    }

    static PKCS8 createFromNative(byte[] bytes) {
        PKCS8 instance = new PKCS8();
        instance.bytes = bytes;
        return instance;
    }


    public static native byte[] encodeED25519PrivateKey(byte[] PrivateKey);
    public static native byte[] decodeED25519PrivateKey(byte[] data);

}
