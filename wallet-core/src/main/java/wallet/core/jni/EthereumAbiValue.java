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

public class EthereumAbiValue {
    private long nativeHandle;

    private EthereumAbiValue() {
        nativeHandle = 0;
    }

    static EthereumAbiValue createFromNative(long nativeHandle) {
        EthereumAbiValue instance = new EthereumAbiValue();
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
    public static native String decodeUInt256(byte[] input);
    public static native String decodeValue(byte[] input, String type);
    public static native String decodeArray(byte[] input, String type);

}

