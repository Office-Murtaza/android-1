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

public class EthereumAbiEncoder {
    private long nativeHandle;

    private EthereumAbiEncoder() {
        nativeHandle = 0;
    }

    static EthereumAbiEncoder createFromNative(long nativeHandle) {
        EthereumAbiEncoder instance = new EthereumAbiEncoder();
        instance.nativeHandle = nativeHandle;
        return instance;
    }


    public static native EthereumAbiFunction buildFunction(String name);
    public static native void deleteFunction(EthereumAbiFunction func_in);
    public static native byte[] encode(EthereumAbiFunction func_in);
    public static native boolean decodeOutput(EthereumAbiFunction func_in, byte[] encoded);

}

