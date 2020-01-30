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

public class AlgorandAddress {
    private long nativeHandle;

    private AlgorandAddress() {
        nativeHandle = 0;
    }

    static AlgorandAddress createFromNative(long nativeHandle) {
        AlgorandAddress instance = new AlgorandAddress();
        instance.nativeHandle = nativeHandle;
        AlgorandAddressPhantomReference.register(instance, nativeHandle);
        return instance;
    }

    static native long nativeCreateWithString(String string);
    static native long nativeCreateWithPublicKey(PublicKey publicKey);
    static native void nativeDelete(long handle);

    public static native boolean equals(AlgorandAddress lhs, AlgorandAddress rhs);
    public static native boolean isValidString(String string);
    public native String description();
    public native byte[] keyHash();

    public AlgorandAddress(String string) {
        nativeHandle = nativeCreateWithString(string);
        if (nativeHandle == 0) {
            throw new InvalidParameterException();
        }

        AlgorandAddressPhantomReference.register(this, nativeHandle);
    }

    public AlgorandAddress(PublicKey publicKey) {
        nativeHandle = nativeCreateWithPublicKey(publicKey);
        if (nativeHandle == 0) {
            throw new InvalidParameterException();
        }

        AlgorandAddressPhantomReference.register(this, nativeHandle);
    }

}

class AlgorandAddressPhantomReference extends java.lang.ref.PhantomReference<AlgorandAddress> {
    private static java.util.Set<AlgorandAddressPhantomReference> references = new HashSet<AlgorandAddressPhantomReference>();
    private static java.lang.ref.ReferenceQueue<AlgorandAddress> queue = new java.lang.ref.ReferenceQueue<AlgorandAddress>();
    private long nativeHandle;

    private AlgorandAddressPhantomReference(AlgorandAddress referent, long nativeHandle) {
        super(referent, queue);
        this.nativeHandle = nativeHandle;
    }

    static void register(AlgorandAddress referent, long nativeHandle) {
        references.add(new AlgorandAddressPhantomReference(referent, nativeHandle));
    }

    public static void doDeletes() {
        AlgorandAddressPhantomReference ref = (AlgorandAddressPhantomReference) queue.poll();
        for (; ref != null; ref = (AlgorandAddressPhantomReference) queue.poll()) {
            AlgorandAddress.nativeDelete(ref.nativeHandle);
            references.remove(ref);
        }
    }
}
