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

public class EthereumAbi {
    private long nativeHandle;

    private EthereumAbi() {
        nativeHandle = 0;
    }

    static EthereumAbi createFromNative(long nativeHandle) {
        EthereumAbi instance = new EthereumAbi();
        instance.nativeHandle = nativeHandle;
        return instance;
    }


    public static native byte[] encode(EthereumAbiFunction fn);
    public static native boolean decodeOutput(EthereumAbiFunction fn, byte[] encoded);
    public static native String decodeCall(byte[] data, String abi);

}

