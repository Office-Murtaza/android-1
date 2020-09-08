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

public class EthereumAbiFunction {
    private long nativeHandle;

    private EthereumAbiFunction() {
        nativeHandle = 0;
    }

    static EthereumAbiFunction createFromNative(long nativeHandle) {
        EthereumAbiFunction instance = new EthereumAbiFunction();
        instance.nativeHandle = nativeHandle;
        EthereumAbiFunctionPhantomReference.register(instance, nativeHandle);
        return instance;
    }

    static native long nativeCreateWithString(String name);
    static native void nativeDelete(long handle);

    public native String getType();
    public native int addParamUInt8(byte val, boolean isOutput);
    public native int addParamUInt16(short val, boolean isOutput);
    public native int addParamUInt32(int val, boolean isOutput);
    public native int addParamUInt64(long val, boolean isOutput);
    public native int addParamUInt256(byte[] val, boolean isOutput);
    public native int addParamUIntN(int bits, byte[] val, boolean isOutput);
    public native int addParamInt8(byte val, boolean isOutput);
    public native int addParamInt16(short val, boolean isOutput);
    public native int addParamInt32(int val, boolean isOutput);
    public native int addParamInt64(long val, boolean isOutput);
    public native int addParamInt256(byte[] val, boolean isOutput);
    public native int addParamIntN(int bits, byte[] val, boolean isOutput);
    public native int addParamBool(boolean val, boolean isOutput);
    public native int addParamString(String val, boolean isOutput);
    public native int addParamAddress(byte[] val, boolean isOutput);
    public native int addParamBytes(byte[] val, boolean isOutput);
    public native int addParamBytesFix(int size, byte[] val, boolean isOutput);
    public native int addParamArray(boolean isOutput);
    public native byte getParamUInt8(int idx, boolean isOutput);
    public native long getParamUInt64(int idx, boolean isOutput);
    public native byte[] getParamUInt256(int idx, boolean isOutput);
    public native boolean getParamBool(int idx, boolean isOutput);
    public native String getParamString(int idx, boolean isOutput);
    public native byte[] getParamAddress(int idx, boolean isOutput);
    public native int addInArrayParamUInt8(int arrayIdx, byte val);
    public native int addInArrayParamUInt16(int arrayIdx, short val);
    public native int addInArrayParamUInt32(int arrayIdx, int val);
    public native int addInArrayParamUInt64(int arrayIdx, long val);
    public native int addInArrayParamUInt256(int arrayIdx, byte[] val);
    public native int addInArrayParamUIntN(int arrayIdx, int bits, byte[] val);
    public native int addInArrayParamInt8(int arrayIdx, byte val);
    public native int addInArrayParamInt16(int arrayIdx, short val);
    public native int addInArrayParamInt32(int arrayIdx, int val);
    public native int addInArrayParamInt64(int arrayIdx, long val);
    public native int addInArrayParamInt256(int arrayIdx, byte[] val);
    public native int addInArrayParamIntN(int arrayIdx, int bits, byte[] val);
    public native int addInArrayParamBool(int arrayIdx, boolean val);
    public native int addInArrayParamString(int arrayIdx, String val);
    public native int addInArrayParamAddress(int arrayIdx, byte[] val);
    public native int addInArrayParamBytes(int arrayIdx, byte[] val);
    public native int addInArrayParamBytesFix(int arrayIdx, int size, byte[] val);

    public EthereumAbiFunction(String name) {
        nativeHandle = nativeCreateWithString(name);
        if (nativeHandle == 0) {
            throw new InvalidParameterException();
        }

        EthereumAbiFunctionPhantomReference.register(this, nativeHandle);
    }

}

class EthereumAbiFunctionPhantomReference extends java.lang.ref.PhantomReference<EthereumAbiFunction> {
    private static java.util.Set<EthereumAbiFunctionPhantomReference> references = new HashSet<EthereumAbiFunctionPhantomReference>();
    private static java.lang.ref.ReferenceQueue<EthereumAbiFunction> queue = new java.lang.ref.ReferenceQueue<EthereumAbiFunction>();
    private long nativeHandle;

    private EthereumAbiFunctionPhantomReference(EthereumAbiFunction referent, long nativeHandle) {
        super(referent, queue);
        this.nativeHandle = nativeHandle;
    }

    static void register(EthereumAbiFunction referent, long nativeHandle) {
        references.add(new EthereumAbiFunctionPhantomReference(referent, nativeHandle));
    }

    public static void doDeletes() {
        EthereumAbiFunctionPhantomReference ref = (EthereumAbiFunctionPhantomReference) queue.poll();
        for (; ref != null; ref = (EthereumAbiFunctionPhantomReference) queue.poll()) {
            EthereumAbiFunction.nativeDelete(ref.nativeHandle);
            references.remove(ref);
        }
    }
}
