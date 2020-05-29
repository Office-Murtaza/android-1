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

public class EthereumAbiValueEncoder {
    private long nativeHandle;

    private EthereumAbiValueEncoder() {
        nativeHandle = 0;
    }

    static EthereumAbiValueEncoder createFromNative(long nativeHandle) {
        EthereumAbiValueEncoder instance = new EthereumAbiValueEncoder();
        instance.nativeHandle = nativeHandle;
        return instance;
    }


    public static native byte[] encodeBool(boolean value);
    public static native byte[] encodeInt32(int value);
    public static native byte[] encodeUInt32(int value);
    public static native byte[] encodeInt256(byte[] value);
    public static native byte[] encodeUInt256(byte[] value);
    public static native byte[] encodeAddress(byte[] value);
    public static native byte[] encodeString(String value);
    public static native byte[] encodeBytes(byte[] value);
    public static native byte[] encodeBytesDyn(byte[] value);

}

