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
import java.util.HashSet;

public class BitcoinAddress {
    private long nativeHandle;

    private BitcoinAddress() {
        nativeHandle = 0;
    }

    static BitcoinAddress createFromNative(long nativeHandle) {
        BitcoinAddress instance = new BitcoinAddress();
        instance.nativeHandle = nativeHandle;
        BitcoinAddressPhantomReference.register(instance, nativeHandle);
        return instance;
    }

    static native long nativeCreateWithString(String string);
    static native long nativeCreateWithData(byte[] data);
    static native long nativeCreateWithPublicKey(PublicKey publicKey, byte prefix);
    static native void nativeDelete(long handle);

    public static native boolean equals(BitcoinAddress lhs, BitcoinAddress rhs);
    public static native boolean isValid(byte[] data);
    public static native boolean isValidString(String string);
    public native String description();
    public native byte prefix();
    public native byte[] keyhash();

    public BitcoinAddress(String string) {
        nativeHandle = nativeCreateWithString(string);
        if (nativeHandle == 0) {
            throw new InvalidParameterException();
        }

        BitcoinAddressPhantomReference.register(this, nativeHandle);
    }

    public BitcoinAddress(byte[] data) {
        nativeHandle = nativeCreateWithData(data);
        if (nativeHandle == 0) {
            throw new InvalidParameterException();
        }

        BitcoinAddressPhantomReference.register(this, nativeHandle);
    }

    public BitcoinAddress(PublicKey publicKey, byte prefix) {
        nativeHandle = nativeCreateWithPublicKey(publicKey, prefix);
        if (nativeHandle == 0) {
            throw new InvalidParameterException();
        }

        BitcoinAddressPhantomReference.register(this, nativeHandle);
    }

}

class BitcoinAddressPhantomReference extends java.lang.ref.PhantomReference<BitcoinAddress> {
    private static java.util.Set<BitcoinAddressPhantomReference> references = new HashSet<BitcoinAddressPhantomReference>();
    private static java.lang.ref.ReferenceQueue<BitcoinAddress> queue = new java.lang.ref.ReferenceQueue<BitcoinAddress>();
    private long nativeHandle;

    private BitcoinAddressPhantomReference(BitcoinAddress referent, long nativeHandle) {
        super(referent, queue);
        this.nativeHandle = nativeHandle;
    }

    static void register(BitcoinAddress referent, long nativeHandle) {
        references.add(new BitcoinAddressPhantomReference(referent, nativeHandle));
    }

    public static void doDeletes() {
        BitcoinAddressPhantomReference ref = (BitcoinAddressPhantomReference) queue.poll();
        for (; ref != null; ref = (BitcoinAddressPhantomReference) queue.poll()) {
            BitcoinAddress.nativeDelete(ref.nativeHandle);
            references.remove(ref);
        }
    }
}
