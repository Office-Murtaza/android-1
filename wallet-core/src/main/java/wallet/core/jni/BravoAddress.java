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

public class BravoAddress {
    private long nativeHandle;

    private BravoAddress() {
        nativeHandle = 0;
    }

    static BravoAddress createFromNative(long nativeHandle) {
        BravoAddress instance = new BravoAddress();
        instance.nativeHandle = nativeHandle;
        BravoAddressPhantomReference.register(instance, nativeHandle);
        return instance;
    }

    static native long nativeCreateWithString(String string);
    static native long nativeCreateWithPublicKey(PublicKey publicKey, BravoAddressType type);
    static native long nativeCreateWithKeyHash(byte[] keyHash, BravoAddressType type);
    static native void nativeDelete(long handle);

    public static native boolean equals(BravoAddress lhs, BravoAddress rhs);
    public static native boolean isValidString(String string);
    public native String description();

    public BravoAddress(String string) {
        nativeHandle = nativeCreateWithString(string);
        if (nativeHandle == 0) {
            throw new InvalidParameterException();
        }

        BravoAddressPhantomReference.register(this, nativeHandle);
    }

    public BravoAddress(PublicKey publicKey, BravoAddressType type) {
        nativeHandle = nativeCreateWithPublicKey(publicKey, type);
        if (nativeHandle == 0) {
            throw new InvalidParameterException();
        }

        BravoAddressPhantomReference.register(this, nativeHandle);
    }

    public BravoAddress(byte[] keyHash, BravoAddressType type) {
        nativeHandle = nativeCreateWithKeyHash(keyHash, type);
        if (nativeHandle == 0) {
            throw new InvalidParameterException();
        }

        BravoAddressPhantomReference.register(this, nativeHandle);
    }

}

class BravoAddressPhantomReference extends java.lang.ref.PhantomReference<BravoAddress> {
    private static java.util.Set<BravoAddressPhantomReference> references = new HashSet<BravoAddressPhantomReference>();
    private static java.lang.ref.ReferenceQueue<BravoAddress> queue = new java.lang.ref.ReferenceQueue<BravoAddress>();
    private long nativeHandle;

    private BravoAddressPhantomReference(BravoAddress referent, long nativeHandle) {
        super(referent, queue);
        this.nativeHandle = nativeHandle;
    }

    static void register(BravoAddress referent, long nativeHandle) {
        references.add(new BravoAddressPhantomReference(referent, nativeHandle));
    }

    public static void doDeletes() {
        BravoAddressPhantomReference ref = (BravoAddressPhantomReference) queue.poll();
        for (; ref != null; ref = (BravoAddressPhantomReference) queue.poll()) {
            BravoAddress.nativeDelete(ref.nativeHandle);
            references.remove(ref);
        }
    }
}
