// Copyright © 2017-2020 Trust Wallet.
//
// This file is part of Trust. The full Trust copyright notice, including
// terms governing use, modification, and redistribution, is contained in the
// file LICENSE at the root of the source code distribution tree.
//
// This is a GENERATED FILE, changes made here WILL BE LOST.
//

package wallet.core.jni;

import java.security.InvalidParameterException;

public class AES {
    private byte[] bytes;

    private AES() {
    }

    static AES createFromNative(byte[] bytes) {
        AES instance = new AES();
        instance.bytes = bytes;
        return instance;
    }


    public static native byte[] encryptCBC(byte[] key, byte[] data, byte[] iv, AESPaddingMode mode);
    public static native byte[] decryptCBC(byte[] key, byte[] data, byte[] iv, AESPaddingMode mode);
    public static native byte[] encryptCTR(byte[] key, byte[] data, byte[] iv);
    public static native byte[] decryptCTR(byte[] key, byte[] data, byte[] iv);

}
