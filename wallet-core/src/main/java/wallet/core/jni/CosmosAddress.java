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

public class CosmosAddress {
    private long nativeHandle;

    private CosmosAddress() {
        nativeHandle = 0;
    }

    static CosmosAddress createFromNative(long nativeHandle) {
        CosmosAddress instance = new CosmosAddress();
        instance.nativeHandle = nativeHandle;
        CosmosAddressPhantomReference.register(instance, nativeHandle);
        return instance;
    }

    static native long nativeCreateWithString(String string);
    static native long nativeCreateWithKeyHash(HRP hrp, byte[] keyHash);
    static native long nativeCreateWithPublicKey(HRP hrp, PublicKey publicKey);
    static native void nativeDelete(long handle);

    public static native boolean equals(CosmosAddress lhs, CosmosAddress rhs);
    public static native boolean isValidString(String string);
    public native String description();
    public native HRP hrp();
    public native byte[] keyHash();

    public CosmosAddress(String string) {
        nativeHandle = nativeCreateWithString(string);
        if (nativeHandle == 0) {
            throw new InvalidParameterException();
        }

        CosmosAddressPhantomReference.register(this, nativeHandle);
    }

    public CosmosAddress(HRP hrp, byte[] keyHash) {
        nativeHandle = nativeCreateWithKeyHash(hrp, keyHash);
        if (nativeHandle == 0) {
            throw new InvalidParameterException();
        }

        CosmosAddressPhantomReference.register(this, nativeHandle);
    }

    public CosmosAddress(HRP hrp, PublicKey publicKey) {
        nativeHandle = nativeCreateWithPublicKey(hrp, publicKey);
        if (nativeHandle == 0) {
            throw new InvalidParameterException();
        }

        CosmosAddressPhantomReference.register(this, nativeHandle);
    }

}

class CosmosAddressPhantomReference extends java.lang.ref.PhantomReference<CosmosAddress> {
    private static java.util.Set<CosmosAddressPhantomReference> references = new HashSet<CosmosAddressPhantomReference>();
    private static java.lang.ref.ReferenceQueue<CosmosAddress> queue = new java.lang.ref.ReferenceQueue<CosmosAddress>();
    private long nativeHandle;

    private CosmosAddressPhantomReference(CosmosAddress referent, long nativeHandle) {
        super(referent, queue);
        this.nativeHandle = nativeHandle;
    }

    static void register(CosmosAddress referent, long nativeHandle) {
        references.add(new CosmosAddressPhantomReference(referent, nativeHandle));
    }

    public static void doDeletes() {
        CosmosAddressPhantomReference ref = (CosmosAddressPhantomReference) queue.poll();
        for (; ref != null; ref = (CosmosAddressPhantomReference) queue.poll()) {
            CosmosAddress.nativeDelete(ref.nativeHandle);
            references.remove(ref);
        }
    }
}
