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
import java.util.HashSet;

public class EthereumAbiValueDecoder {
    private long nativeHandle;

    private EthereumAbiValueDecoder() {
        nativeHandle = 0;
    }

    static EthereumAbiValueDecoder createFromNative(long nativeHandle) {
        EthereumAbiValueDecoder instance = new EthereumAbiValueDecoder();
        instance.nativeHandle = nativeHandle;
        return instance;
    }


    public static native String decodeUInt256(byte[] input);

}

