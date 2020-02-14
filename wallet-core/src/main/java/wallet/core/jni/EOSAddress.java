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

public class EOSAddress {
    private long nativeHandle;

    private EOSAddress() {
        nativeHandle = 0;
    }

    static EOSAddress createFromNative(long nativeHandle) {
        EOSAddress instance = new EOSAddress();
        instance.nativeHandle = nativeHandle;
        EOSAddressPhantomReference.register(instance, nativeHandle);
        return instance;
    }

    static native long nativeCreateWithString(String string);
    static native long nativeCreateWithPublicKey(PublicKey publicKey, EOSKeyType type);
    static native long nativeCreateWithKeyHash(byte[] keyHash, EOSKeyType type);
    static native void nativeDelete(long handle);

    public static native boolean equals(EOSAddress lhs, EOSAddress rhs);
    public static native boolean isValidString(String string);
    public native String description();

    public EOSAddress(String string) {
        nativeHandle = nativeCreateWithString(string);
        if (nativeHandle == 0) {
            throw new InvalidParameterException();
        }

        EOSAddressPhantomReference.register(this, nativeHandle);
    }

    public EOSAddress(PublicKey publicKey, EOSKeyType type) {
        nativeHandle = nativeCreateWithPublicKey(publicKey, type);
        if (nativeHandle == 0) {
            throw new InvalidParameterException();
        }

        EOSAddressPhantomReference.register(this, nativeHandle);
    }

    public EOSAddress(byte[] keyHash, EOSKeyType type) {
        nativeHandle = nativeCreateWithKeyHash(keyHash, type);
        if (nativeHandle == 0) {
            throw new InvalidParameterException();
        }

        EOSAddressPhantomReference.register(this, nativeHandle);
    }

}

class EOSAddressPhantomReference extends java.lang.ref.PhantomReference<EOSAddress> {
    private static java.util.Set<EOSAddressPhantomReference> references = new HashSet<EOSAddressPhantomReference>();
    private static java.lang.ref.ReferenceQueue<EOSAddress> queue = new java.lang.ref.ReferenceQueue<EOSAddress>();
    private long nativeHandle;

    private EOSAddressPhantomReference(EOSAddress referent, long nativeHandle) {
        super(referent, queue);
        this.nativeHandle = nativeHandle;
    }

    static void register(EOSAddress referent, long nativeHandle) {
        references.add(new EOSAddressPhantomReference(referent, nativeHandle));
    }

    public static void doDeletes() {
        EOSAddressPhantomReference ref = (EOSAddressPhantomReference) queue.poll();
        for (; ref != null; ref = (EOSAddressPhantomReference) queue.poll()) {
            EOSAddress.nativeDelete(ref.nativeHandle);
            references.remove(ref);
        }
    }
}
