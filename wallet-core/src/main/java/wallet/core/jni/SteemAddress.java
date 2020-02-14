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

public class SteemAddress {
    private long nativeHandle;

    private SteemAddress() {
        nativeHandle = 0;
    }

    static SteemAddress createFromNative(long nativeHandle) {
        SteemAddress instance = new SteemAddress();
        instance.nativeHandle = nativeHandle;
        SteemAddressPhantomReference.register(instance, nativeHandle);
        return instance;
    }

    static native long nativeCreateWithString(String string);
    static native long nativeCreateWithPublicKey(PublicKey publicKey, BravoAddressType type);
    static native long nativeCreateWithKeyHash(byte[] keyHash, BravoAddressType type);
    static native void nativeDelete(long handle);

    public static native boolean equals(SteemAddress lhs, SteemAddress rhs);
    public static native boolean isValidString(String string);
    public native String description();

    public SteemAddress(String string) {
        nativeHandle = nativeCreateWithString(string);
        if (nativeHandle == 0) {
            throw new InvalidParameterException();
        }

        SteemAddressPhantomReference.register(this, nativeHandle);
    }

    public SteemAddress(PublicKey publicKey, BravoAddressType type) {
        nativeHandle = nativeCreateWithPublicKey(publicKey, type);
        if (nativeHandle == 0) {
            throw new InvalidParameterException();
        }

        SteemAddressPhantomReference.register(this, nativeHandle);
    }

    public SteemAddress(byte[] keyHash, BravoAddressType type) {
        nativeHandle = nativeCreateWithKeyHash(keyHash, type);
        if (nativeHandle == 0) {
            throw new InvalidParameterException();
        }

        SteemAddressPhantomReference.register(this, nativeHandle);
    }

}

class SteemAddressPhantomReference extends java.lang.ref.PhantomReference<SteemAddress> {
    private static java.util.Set<SteemAddressPhantomReference> references = new HashSet<SteemAddressPhantomReference>();
    private static java.lang.ref.ReferenceQueue<SteemAddress> queue = new java.lang.ref.ReferenceQueue<SteemAddress>();
    private long nativeHandle;

    private SteemAddressPhantomReference(SteemAddress referent, long nativeHandle) {
        super(referent, queue);
        this.nativeHandle = nativeHandle;
    }

    static void register(SteemAddress referent, long nativeHandle) {
        references.add(new SteemAddressPhantomReference(referent, nativeHandle));
    }

    public static void doDeletes() {
        SteemAddressPhantomReference ref = (SteemAddressPhantomReference) queue.poll();
        for (; ref != null; ref = (SteemAddressPhantomReference) queue.poll()) {
            SteemAddress.nativeDelete(ref.nativeHandle);
            references.remove(ref);
        }
    }
}
