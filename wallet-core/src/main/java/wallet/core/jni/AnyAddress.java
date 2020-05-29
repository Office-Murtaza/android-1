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

public class AnyAddress {
    private long nativeHandle;

    private AnyAddress() {
        nativeHandle = 0;
    }

    static AnyAddress createFromNative(long nativeHandle) {
        AnyAddress instance = new AnyAddress();
        instance.nativeHandle = nativeHandle;
        AnyAddressPhantomReference.register(instance, nativeHandle);
        return instance;
    }

    static native long nativeCreateWithString(String string, CoinType coin);
    static native long nativeCreateWithPublicKey(PublicKey publicKey, CoinType coin);
    static native void nativeDelete(long handle);

    public static native boolean equals(AnyAddress lhs, AnyAddress rhs);
    public static native boolean isValid(String string, CoinType coin);
    public native String description();
    public native CoinType coin();
    public native byte[] data();

    public AnyAddress(String string, CoinType coin) {
        nativeHandle = nativeCreateWithString(string, coin);
        if (nativeHandle == 0) {
            throw new InvalidParameterException();
        }

        AnyAddressPhantomReference.register(this, nativeHandle);
    }

    public AnyAddress(PublicKey publicKey, CoinType coin) {
        nativeHandle = nativeCreateWithPublicKey(publicKey, coin);
        if (nativeHandle == 0) {
            throw new InvalidParameterException();
        }

        AnyAddressPhantomReference.register(this, nativeHandle);
    }

}

class AnyAddressPhantomReference extends java.lang.ref.PhantomReference<AnyAddress> {
    private static java.util.Set<AnyAddressPhantomReference> references = new HashSet<AnyAddressPhantomReference>();
    private static java.lang.ref.ReferenceQueue<AnyAddress> queue = new java.lang.ref.ReferenceQueue<AnyAddress>();
    private long nativeHandle;

    private AnyAddressPhantomReference(AnyAddress referent, long nativeHandle) {
        super(referent, queue);
        this.nativeHandle = nativeHandle;
    }

    static void register(AnyAddress referent, long nativeHandle) {
        references.add(new AnyAddressPhantomReference(referent, nativeHandle));
    }

    public static void doDeletes() {
        AnyAddressPhantomReference ref = (AnyAddressPhantomReference) queue.poll();
        for (; ref != null; ref = (AnyAddressPhantomReference) queue.poll()) {
            AnyAddress.nativeDelete(ref.nativeHandle);
            references.remove(ref);
        }
    }
}
